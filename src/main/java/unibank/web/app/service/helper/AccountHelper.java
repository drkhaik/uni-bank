package unibank.web.app.service.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import unibank.web.app.dto.AccountDto;
import unibank.web.app.entity.*;
import unibank.web.app.repository.AccountRepository;
import unibank.web.app.repository.TransactionRepository;
import unibank.web.app.uitl.RandomUtil;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Getter
public class AccountHelper {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

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
                .balance(1000)
                .owner(user)
                .code(accountDto.getCode())
                .symbol(SYMBOLS.get(accountDto.getCode()).charAt(0))
                .label(CURRENCIES.get(accountDto.getCode()))
                .build();

        return accountRepository.save(account);
    }

    public Transaction performTransfer(Account senderAccount, Account receiverAccount, double amount, User user) throws Exception {
        validateSufficientFunds(senderAccount, (amount*1.01));
        senderAccount.setBalance(senderAccount.getBalance() - amount*1.01);
        receiverAccount.setBalance(receiverAccount.getBalance() + amount);
        accountRepository.saveAll(List.of(senderAccount, receiverAccount));
        var senderTransaction = Transaction.builder()
                .account(senderAccount)
                .status(Status.COMPLETED)
                .type(Type.WITHDRAW)
                .txFee(amount * 0.01)
                .amount(amount)
                .owner(senderAccount.getOwner())
                .build();
        var receiverTransaction = Transaction.builder()
                .account(senderAccount)
                .status(Status.COMPLETED)
                .type(Type.DEPOSIT)
                .amount(amount)
                .owner(receiverAccount.getOwner())
                .build();
        return transactionRepository.saveAll(List.of(senderTransaction, receiverTransaction)).getFirst();
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

}
