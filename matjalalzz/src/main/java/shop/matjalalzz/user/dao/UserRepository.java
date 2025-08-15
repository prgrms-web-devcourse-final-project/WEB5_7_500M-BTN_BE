package shop.matjalalzz.user.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.user.dto.LoginInfoDto;
import shop.matjalalzz.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthId(String oauthId);
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT new shop.matjalalzz.user.dto.LoginInfoDto(
                u.id, u.password, u.role, u.email, rt.refreshToken)
        FROM User u
            LEFT JOIN RefreshToken rt on u.id = rt.user.id
        WHERE u.email = :email
        """)
    Optional<LoginInfoDto> findByEmailForLogin(@Param("email") String email);
}
