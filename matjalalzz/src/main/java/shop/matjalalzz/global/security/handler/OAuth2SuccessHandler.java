package shop.matjalalzz.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.app.TokenService;
import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponseDto;
import shop.matjalalzz.global.util.CookieUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;

    @Value("${custom.jwt.redirect-login-success}")
    private String redirectSuccess;

    @Value("${custom.jwt.token-validity-time.refresh}")
    private int refreshTokenValidityTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        PrincipalUser userInfo = (PrincipalUser) authentication.getPrincipal();

        LoginTokenResponseDto dto = tokenService.oauthLogin(userInfo.getUsername());

        response.setHeader("Authorization", "Bearer " + dto.accessToken());
        CookieUtils.setRefreshTokenCookie(response, dto.refreshToken(), refreshTokenValidityTime);
        response.sendRedirect(redirectSuccess);
    }
}
