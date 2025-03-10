package unibank.web.app.service.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import unibank.web.app.dto.AccountDto;
import unibank.web.app.dto.ConvertDto;
import unibank.web.app.entity.*;
import unibank.web.app.repository.AccountRepository;
import unibank.web.app.repository.TransactionRepository;
import unibank.web.app.service.ExchangeRateService;
import unibank.web.app.service.TransactionService;
import unibank.web.app.util.RandomUtil;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Getter
public class AccountHelper {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final ExchangeRateService exchangeRateService;

    private final Map<String, String> CURRENCIES = Map.of(
            "USD", "United States Dollar",
            "EUR", "Euro",
            "GBP", "British Pound",
            "JPY", "Japanese Yen",
            "NGN", "Nigerian Naira",
            "INR", "Indian Rupee",
            "VND", "Vietnamese Dong"
    );

    private final Map<String, String> SYMBOLS = Map.of(
            "USD", "$",
            "EUR", "€",
            "GBP", "£",
            "JPY", "¥",
            "NGN", "₦",
            "INR", "₹",
            "VND", "₫"
    );

    public Account createAccount(AccountDto accountDto, User user) {
        long accountNumber;
        try {
            validateAccountNonExistForUser(accountDto.getCode(), user.getUid());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        do{
            accountNumber = new RandomUtil().generateRandom(10);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        var account =  Account.builder()
                .accountNumber(accountNumber)
                .accountName(user.getFirstname() + " " + user.getLastname())
                .balance(1000)
                .owner(user)
                .code(accountDto.getCode())
                .symbol(SYMBOLS.get(accountDto.getCode()).charAt(0))
                .label(CURRENCIES.get(accountDto.getCode()))
                .build();

        return accountRepository.save(account);
    }

    public Transaction performTransfer(
            Account senderAccount,
            Account receiverAccount,
            double amount,
            User user
    ) throws Exception {
        validateSufficientFunds(senderAccount, (amount * 1.01));
        senderAccount.setBalance(senderAccount.getBalance() - amount * 1.01);
        receiverAccount.setBalance(receiverAccount.getBalance() + amount);
        accountRepository.saveAll(List.of(senderAccount, receiverAccount));

        var senderTransaction = transactionService.createAccountTransaction(
                amount,
                Type.WITHDRAW,
                amount * 0.01,
                user,
                senderAccount
        );
        var receiverTransaction = transactionService.createAccountTransaction(
                amount,
                Type.DEPOSIT,
                0.00,
                receiverAccount.getOwner(),
                receiverAccount
        );
        return senderTransaction;
    }

    public void validateAccountNonExistForUser(String code, String uid) throws Exception {
        if(accountRepository.existsByCodeAndOwnerUid(code, uid)){
            throw new Exception("Account of this type already exists for this user.");
        }
    }

    public void validateAccountOwner(Account account,User user) throws OperationNotSupportedException {
        if (!account.getOwner().getUid().equals(user.getUid())) {
            throw new OperationNotSupportedException("Invalid account owner");
        }
    }

    public void validateSufficientFunds(Account account, double amount) throws Exception {
        if (account.getBalance() < amount) {
            throw new OperationNotSupportedException("Insufficient funds in the account");
        }
    }

    public void validateAmount(double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }

    public void validateDifferentCurrencyType(ConvertDto convertDto) throws Exception {
        if(convertDto.getFromCurrency().equals(convertDto.getToCurrency())){
            throw new IllegalArgumentException("Cannot convert same currency type");
        }
    }

    public void validateAccountOwnership(ConvertDto convertDto, String uid) throws Exception {
        accountRepository.findByCodeAndOwnerUid(convertDto.getFromCurrency(), uid).orElseThrow();
        accountRepository.findByCodeAndOwnerUid(convertDto.getToCurrency(), uid).orElseThrow();
    }

    public void validateConversion(ConvertDto convertDto, String uid) throws Exception {
        validateDifferentCurrencyType(convertDto);
        validateAccountOwnership(convertDto, uid);
        validateAmount(convertDto.getAmount());
        validateSufficientFunds(
                accountRepository.findByCodeAndOwnerUid(convertDto.getFromCurrency(),uid).get(),
                convertDto.getAmount()
        );
    }

    public Transaction convertCurrency(ConvertDto convertDto, User user) throws Exception {
        validateConversion(convertDto, user.getUid());
        var rates = exchangeRateService.getRates();
        var sendingRates = rates.get(convertDto.getFromCurrency());
        var receivingRates = rates.get(convertDto.getToCurrency());
        var computedAmount = (receivingRates/sendingRates) * convertDto.getAmount();
        Account fromAccount = accountRepository.findByCodeAndOwnerUid(convertDto.getFromCurrency(), user.getUid()).orElseThrow();
        Account toAccount = accountRepository.findByCodeAndOwnerUid(convertDto.getToCurrency(), user.getUid()).orElseThrow();
        fromAccount.setBalance(fromAccount.getBalance() - (convertDto.getAmount() * 1.01));
        toAccount.setBalance(toAccount.getBalance() + computedAmount);
        accountRepository.saveAll(List.of(fromAccount, toAccount));

        var fromAccountTransaction = transactionService.createAccountTransaction(
                convertDto.getAmount(),
                Type.CONVERSION,
                convertDto.getAmount() * 0.01,
                user,
                fromAccount
        );
        var toAccountTransaction = transactionService.createAccountTransaction(
                computedAmount,
                Type.DEPOSIT,
                convertDto.getAmount() * 0.00,
                user,
                toAccount
        );
        return fromAccountTransaction;
    }

    public Account save(Account usdAccount) {
        return accountRepository.save(usdAccount);
    }
}
