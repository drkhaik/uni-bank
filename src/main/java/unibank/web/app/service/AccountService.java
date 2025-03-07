package unibank.web.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import unibank.web.app.dto.AccountDto;
import unibank.web.app.entity.Account;
import unibank.web.app.entity.User;
import unibank.web.app.repository.AccountRepository;
import unibank.web.app.service.helper.AccountHelper;
import unibank.web.app.uitl.RandomUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountHelper accountHelper;

    public Account createAccount(AccountDto accountDto, User user) {
        return accountHelper.createAccount(accountDto, user);
    }

    public List<Account> getUserAccounts(String uid) {
        return accountRepository.findAllByOwnerUid(uid);
    }
}
