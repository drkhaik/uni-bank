package unibank.web.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import unibank.web.app.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findAllByOwnerUid(String uid, Pageable pageable);

    Page<Transaction> findAllByAccountAccountIdAndOwnerUid(String accountId, String uid, Pageable pageable);

    Page<Transaction> findAllByCardCardIdAndOwnerUid(String cardId, String uid, Pageable pageable);

//    List<Transaction> findByCard_CardIdAndOwner_Uid(String cardId, String uid);


}
