package unibank.web.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unibank.web.app.dto.AccountDto;
import unibank.web.app.dto.ConvertDto;
import unibank.web.app.dto.TransferDto;
import unibank.web.app.entity.Account;
import unibank.web.app.entity.Transaction;
import unibank.web.app.entity.User;
import unibank.web.app.repository.AccountRepository;
import unibank.web.app.service.helper.AccountHelper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountHelper accountHelper;
    private final ExchangeRateService exchangeRateService;

    public Account createAccount(AccountDto accountDto, User user) {
        return accountHelper.createAccount(accountDto, user);
    }

    public List<Account> getUserAccounts(String uid) {
        return accountRepository.findAllByOwnerUid(uid);
    }

    public Transaction transferFunds(TransferDto transferDto, User user) throws Exception {
        var senderAccount = accountRepository.findByCodeAndOwnerUid(transferDto.getCode(), user.getUid())
                .orElseThrow(() -> new UnsupportedOperationException("Account of type currency do not exists for user "));
        System.out.println("Sender Account: " + senderAccount);
        
        var receiverAccount = accountRepository.findByAccountNumber(transferDto.getRecipientAccountNumber()).orElseThrow();
        return accountHelper.performTransfer(senderAccount, receiverAccount, transferDto.getAmount(), user);
    }

    public Map<String, Double> getExchangeRate() {
        return exchangeRateService.getRates();
    }

    public Transaction convertCurrency(ConvertDto convertDto, User user) throws Exception {
        return accountHelper.convertCurrency(convertDto, user);
    }

}
