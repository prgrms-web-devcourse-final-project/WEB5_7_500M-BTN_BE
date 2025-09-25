package shop.matjalalzz.user.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.user.dto.projection.LoginUserProjection;
import shop.matjalalzz.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {

    Optional<User> findByOauthId(String oauthId);

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u.id AS userId, u.password AS password, u.role AS role, u.email AS email,
               rt.refreshToken AS refreshToken
        FROM User u
            LEFT JOIN RefreshToken rt on u.id = rt.user.id
        WHERE u.email = :email
        """)
    Optional<LoginUserProjection> findByEmailForLogin(@Param("email") String email);
}
