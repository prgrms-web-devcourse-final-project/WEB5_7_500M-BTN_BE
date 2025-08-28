package shop.matjalalzz.global.security.jwt.app;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.jwt.dao.RefreshTokenRepository;
import shop.matjalalzz.global.security.jwt.dto.AccessTokenResponse;
import shop.matjalalzz.global.security.jwt.dto.projection.AuthUserProjection;
import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponse;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.global.security.jwt.mapper.TokenMapper;
import shop.matjalalzz.global.util.CookieUtils;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.dto.projection.LoginUserProjection;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    public LoginTokenResponse oauthLogin(String email) {
        LoginUserProjection found = userRepository.findByEmailForLogin(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND));

        String accessToken = tokenProvider.issueAccessToken(
            found.getUserId(), found.getRole(), found.getEmail()
        );

        String newRefreshToken = tokenProvider.issueRefreshToken(found.getUserId());

        refreshTokenRepository.upsertByUserId(found.getUserId(), newRefreshToken);

        return TokenMapper.toLoginTokenResponseDto(accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(long userId, HttpServletResponse response) {
        refreshTokenRepository.findById(userId).ifPresent(refreshTokenRepository::delete);

        CookieUtils.deleteRefreshTokenCookie(response);
    }

    @Transactional
    public void upsertRefreshToken(long userId, String refreshToken) {
        refreshTokenRepository.upsertByUserId(userId, refreshToken);
    }

    @Transactional
    public void deleteRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    @Transactional(readOnly = true)
    public AccessTokenResponse refreshAccessToken(String refreshToken) {
        if (!tokenProvider.validate(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = tokenProvider.parseRefreshToken(refreshToken);
        AuthUserProjection info = refreshTokenRepository.findByUserIdWithUser(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!refreshToken.equals(info.getRefreshToken())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 새로운 액세스 토큰 발급
        String newAccessToken = tokenProvider.issueAccessToken(userId, info.getRole(),
            info.getEmail());
        return new AccessTokenResponse(newAccessToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findRefreshToken(User user) {
        return refreshTokenRepository.findByUser(user);
    }

}
