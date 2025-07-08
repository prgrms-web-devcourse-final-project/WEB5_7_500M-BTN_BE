package shop.matjalalzz.domain.user.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.domain.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthId(String oauthId);
    Optional<User> findByEmail(String email);
}
