package unibank.web.app.service;

import unibank.web.app.entity.*;
import unibank.web.app.repository.AccountRepository;
import unibank.web.app.repository.CardRepository;
import unibank.web.app.service.helper.AccountHelper;
import unibank.web.app.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * This class provides services related to credit/debit cards.
 * It interacts with the database to perform operations like creating a new card,
 * crediting/debiting the card, and retrieving card details.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final AccountHelper accountHelper;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;


    public Card getCard(User user) {
        return cardRepository.findByOwnerUid(user.getUid()).orElseThrow();
    }


    public Card createCard(double amount, User user) throws Exception {
        if(amount < 2) {
            throw new IllegalArgumentException("Amount should be at least $2");
        }
        if(!accountRepository.existsByCodeAndOwnerUid("USD", user.getUid())) {
            throw new IllegalArgumentException("USD Account not found for this user so card cannot be created");
        }
        var usdAccount = accountRepository.findByCodeAndOwnerUid("USD", user.getUid()).orElseThrow();
        accountHelper.validateSufficientFunds(usdAccount, amount);
        usdAccount.setBalance(usdAccount.getBalance() - amount);
        long cardNumber;
        do{
            cardNumber = generateCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));
        Card card = Card.builder()
                .cardHolder(user.getFirstname() + " " + user.getLastname())
                .cardNumber(cardNumber)
                .exp(LocalDateTime.now().plusYears(3))
                .owner(user)
                .cvv(new RandomUtil().generateRandom(3).toString())
                .balance(amount - 1)
                .pin(new RandomUtil().generateRandom(6).toString())
                .build();
        card = cardRepository.save(card);
        transactionService.createAccountTransaction(1, Type.WITHDRAW, 0.00, user, usdAccount);
        transactionService.createAccountTransaction(amount-1, Type.WITHDRAW, 0.00, user, usdAccount);
        transactionService.createCardTransaction(amount-1, Type.DEPOSIT, 0.00, user, card);
        accountRepository.save(usdAccount);
        return card;
    }

    private long generateCardNumber() {
        return new RandomUtil().generateRandom(16);
    }


    public Transaction creditCard(double amount, User user) {
        var usdAccount = accountRepository.findByCodeAndOwnerUid("USD", user.getUid()).orElseThrow();
        usdAccount.setBalance(usdAccount.getBalance() - amount);
        transactionService.createAccountTransaction(amount, Type.WITHDRAW, 0.00, user, usdAccount);
        var card = user.getCard();
        card.setBalance(card.getBalance() + amount);
        cardRepository.save(card);
        return transactionService.createCardTransaction(amount, Type.CREDIT, 0.00, user, card);
    }


    public Transaction debitCard(double amount, User user) {
        var usdAccount = accountRepository.findByCodeAndOwnerUid("USD", user.getUid()).orElseThrow();
        usdAccount.setBalance(usdAccount.getBalance() + amount);
        transactionService.createAccountTransaction(amount, Type.DEPOSIT, 0.00, user, usdAccount);
        var card = user.getCard();
        card.setBalance(card.getBalance() - amount);
        cardRepository.save(card);
        return transactionService.createCardTransaction(amount, Type.DEBIT, 0.00, user, card);
    }
}
