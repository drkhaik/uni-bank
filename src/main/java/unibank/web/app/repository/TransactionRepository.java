package unibank.web.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unibank.web.app.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {


}
