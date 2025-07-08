package shop.matjalalzz.global.security.jwt.dao;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.user.domain.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user u WHERE u.id = :userId")
    Optional<RefreshToken> findByUserIdWithUser(@Param("userId") Long userId);

}
