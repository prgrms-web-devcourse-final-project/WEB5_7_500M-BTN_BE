package shop.matjalalzz.global.security.jwt.app;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.jwt.mapper.TokenMapper;
import shop.matjalalzz.user.entity.enums.Role;

@Slf4j
@Component
public class TokenProvider {

    @Value("${custom.jwt.token-validity-time.access}")
    private int accessTokenValiditySeconds;
    @Value("${custom.jwt.token-validity-time.refresh}")
    private int refreshTokenValiditySeconds;
    @Value("${custom.jwt.secret}")
    private String secret;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    //access 토큰 생성
    public String issueAccessToken(Long id, Role role, String email) {
        return issueToken(id, role, accessTokenValiditySeconds, email);
    }

    //refresh 토큰 생성
    public String issueRefreshToken(Long id) {
        return issueToken(id, null, refreshTokenValiditySeconds, null);
    }

    //access & refresh 토큰 생성
    private String issueToken(Long id, Role role, int validitySeconds, String email) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + validitySeconds * 1000);

        JwtBuilder jwtBuilder = Jwts.builder().subject(id.toString());

        if (role != null) {
            jwtBuilder.claim("role", role.name());
        }

        if (email != null) {
            jwtBuilder.claim("email", email);
        }

        return jwtBuilder
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(secretKey, Jwts.SIG.HS256) //문자열을 바이트로 바꿈,  서명으로 어떻게 암호화 할지
            .compact();
    }

    // ** 토큰이 유효한지 아닌지 확인 유효성 검사로 access 토큰이 들어오든 refresh 토큰이 들어오든 상관이 없다 그저 형식, 만료 여부만 검사 **
    public boolean validate(String token) {
        try {
            //검증용 생성
            JwtParser jwtParser = Jwts.parser()
                //내가 만든 키가 맞는지 테스트를 위해 내 비밀키로 설정함
                .verifyWith(secretKey)
                .build();

            //이전에 설정한 시크릿 키를 가져와서 access토큰을 만든 후 만들어진 jwtparser과 token값을 비교해 토큰값이 제대로 맞는지 확인
            // 이 부분에서 서명이 변조 되었는지, 만료 시간을 현재 시간과 비교해 유효한지 판단
            jwtParser.parseSignedClaims(token);

            return true; // 성공하면 유효한 토큰이다

        } catch (ExpiredJwtException e) {
            log.debug("토큰 만료됨: {} - {}", maskToken(token), e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰 형식: {} - {}", maskToken(token), e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("구조가 잘못된 토큰: {} - {}", maskToken(token), e.getMessage());
        } catch (JwtException e) {
            log.error("잘못된 토큰 입력됨: {} - {}", maskToken(token), e.getMessage());
        } catch (Exception e) {
            log.error("알 수 없는 오류: {} - {}", maskToken(token), e.getMessage(), e);
        }

        return false;
    }

    public TokenBodyDto parseJwt(String token) {
        Jws<Claims> parsed = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token);

        String userId = parsed.getPayload().getSubject();
        String role = parsed.getPayload().get("role").toString();
        String email = parsed.getPayload().get("email").toString();

        return TokenMapper.toTokenBodyDto(Long.parseLong(userId), email, Role.valueOf(role));
    }

    //log에서 마스킹 후 출력용
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "(short token)";
        }
        return token.substring(0, 10) + "...(masked)";
    }
}
