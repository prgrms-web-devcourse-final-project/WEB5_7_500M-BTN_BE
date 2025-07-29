package shop.matjalalzz.global.security.jwt.app;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.jwt.dao.RefreshTokenRepository;
import shop.matjalalzz.global.security.jwt.dto.AccessTokenResponseDto;
import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponseDto;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.global.security.jwt.mapper.TokenMapper;
import shop.matjalalzz.global.util.CookieUtils;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Value("${custom.jwt.token-validity-time.refresh}")
    private int refreshTokenValidityTime;

    @Transactional
    public LoginTokenResponseDto oauthLogin(String email) {
        User found = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = tokenProvider.issueAccessToken(found.getId(), found.getRole(),
            found.getEmail());

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

        Long userId = tokenProvider.parseRefreshToken(refreshToken);
        RefreshToken token = refreshTokenRepository.findByUserIdWithUser(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        User user = token.getUser();

        // 새로운 액세스 토큰 발급
        String newAccessToken = tokenProvider.issueAccessToken(user.getId(), user.getRole(),
            user.getEmail());
        return new AccessTokenResponseDto(newAccessToken);
    }

    @Transactional
    public void logout(long userId, HttpServletResponse response) {
        refreshTokenRepository.findById(userId).ifPresent(refreshTokenRepository::delete);

        CookieUtils.deleteRefreshTokenCookie(response);
    }

    @Transactional
    public void setCookie(String accessToken, HttpServletResponse response) {
        if(!tokenProvider.validate(accessToken)) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        TokenBodyDto tokenBodyDto = tokenProvider.parseAccessToken(accessToken);

        User user = userRepository.findById(tokenBodyDto.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
            .orElseGet(() -> refreshTokenRepository.save(
                TokenMapper.toRefreshToken(
                    tokenProvider.issueRefreshToken(user.getId()), user
                )
            ));

        if (!tokenProvider.validate(refreshToken.getRefreshToken())) {
            String reissueRefreshToken = tokenProvider.issueRefreshToken(user.getId());
            refreshToken.updateRefreshToken(reissueRefreshToken);
        }

        CookieUtils.setRefreshTokenCookie(response, refreshToken.getRefreshToken(),
            refreshTokenValidityTime);
    }

}
