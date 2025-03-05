package unibank.web.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import unibank.web.app.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

    User findByUserNameIgnoreCase(String userName);
}
