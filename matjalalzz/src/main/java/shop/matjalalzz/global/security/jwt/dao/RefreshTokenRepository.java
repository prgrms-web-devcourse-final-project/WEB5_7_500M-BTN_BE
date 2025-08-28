package shop.matjalalzz.global.security.jwt.dao;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.global.security.jwt.dto.projection.AuthUserProjection;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.user.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    @Query("""
        SELECT rt.refreshToken as refreshToken, u.role as role, u.email as email
        FROM RefreshToken rt
            JOIN rt.user u
        WHERE rt.user.id = :userId
        """)
    Optional<AuthUserProjection> findByUserIdWithUser(@Param("userId") Long userId);

    @Modifying
    @Query(value = """
        INSERT INTO refresh_token (refresh_token, user_id)
        VALUES (:refreshToken, :userId)
        ON DUPLICATE KEY UPDATE
            refresh_token = :refreshToken
        """, nativeQuery = true)
    void upsertByUserId(@Param("userId") Long userId, @Param("refreshToken") String refreshToken);
}
