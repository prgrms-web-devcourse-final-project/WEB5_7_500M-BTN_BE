package shop.matjalalzz.global.security.jwt.app;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.jwt.dao.RefreshTokenRepository;
import shop.matjalalzz.global.security.jwt.dto.AccessTokenResponseDto;
import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponseDto;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.global.security.jwt.mapper.TokenMapper;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public LoginTokenResponseDto oauthLogin(String email) {
        User found = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = tokenProvider.issueAccessToken(found.getId(), found.getRole(), found.getEmail());

        RefreshToken refreshToken = refreshTokenRepository.findByUser(found)
            .orElseGet(() -> refreshTokenRepository.save(
                TokenMapper.toRefreshToken(
                    tokenProvider.issueRefreshToken(found.getId()), found
                )
            ));

        if (!tokenProvider.validate(refreshToken.getRefreshToken())) {
            String reissueRefreshToken = tokenProvider.issueRefreshToken(found.getId());
            refreshToken.updateRefreshToken(reissueRefreshToken);
        }

        return TokenMapper.toLoginTokenResponseDto(accessToken, refreshToken.getRefreshToken());
    }

    @Transactional(readOnly = true)
    public AccessTokenResponseDto refreshAccessToken(String refreshToken) {
        if (!tokenProvider.validate(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = tokenProvider.parseJwt(refreshToken).userId();
        RefreshToken token = refreshTokenRepository.findByUserIdWithUser(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        User user = token.getUser();

        // 새로운 액세스 토큰 발급
        String newAccessToken = tokenProvider.issueAccessToken(user.getId(), user.getRole(), user.getEmail());
        return new AccessTokenResponseDto(newAccessToken);
    }

    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {
        if (!tokenProvider.validate(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = tokenProvider.parseJwt(refreshToken).userId();
        RefreshToken token = refreshTokenRepository.findByUserIdWithUser(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        refreshTokenRepository.delete(token);

        // 쿠키 삭제 처리
        Cookie cookie = new Cookie("refreshToken", null); // 이름 동일, 값은 null
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        //cookie.setSecure(true); // HTTPS 환경
        cookie.setSecure(false); // HTTP 환경
        cookie.setMaxAge(0); // 즉시 만료
        //cookie.setDomain("your-domain.com"); // 필요 시 설정
        response.addCookie(cookie);
    }

}
