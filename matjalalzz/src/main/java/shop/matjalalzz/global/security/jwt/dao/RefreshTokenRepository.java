package shop.matjalalzz.global.security.jwt.dao;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.global.security.jwt.dto.AuthUserInfoDto;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.user.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

//    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user u WHERE u.id = :userId")
//    Optional<RefreshToken> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("""
        SELECT new shop.matjalalzz.global.security.jwt.dto.AuthUserInfoDto(u.role, u.email)
        FROM RefreshToken rt
            JOIN rt.user u
        WHERE rt.user.id = :userId
    """)
    Optional<AuthUserInfoDto> findByUserIdWithUser(@Param("userId") Long userId);
}
