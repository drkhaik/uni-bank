package unibank.web.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unibank.web.app.entity.Card;

public interface CardRepository extends JpaRepository<Card, String> {


}
