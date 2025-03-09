package unibank.web.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unibank.web.app.entity.Card;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, String> {
    Optional<Card> findByOwnerUid(String uid);

    boolean existsByCardNumber(double cardNumber);
}
