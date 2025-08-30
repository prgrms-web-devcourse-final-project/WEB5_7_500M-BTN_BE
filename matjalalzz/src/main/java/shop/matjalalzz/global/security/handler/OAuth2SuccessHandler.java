package shop.matjalalzz.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.app.TokenService;
import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponse;
import shop.matjalalzz.global.util.CookieUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;

    @Value("${custom.jwt.redirect-login-success}")
    private String redirectSuccess;

    @Value("${custom.jwt.token-validity-time.refresh}")
    private int refreshTokenValidityTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {

        PrincipalUser userInfo = (PrincipalUser) authentication.getPrincipal();

        LoginTokenResponse dto = tokenService.oauthLogin(userInfo.getUsername());

        CookieUtils.setRefreshTokenCookie(response, dto.refreshToken(), refreshTokenValidityTime);

        super.clearAuthenticationAttributes(request);

        String target = UriComponentsBuilder.fromUriString(redirectSuccess)
            .queryParam("accessToken", dto.accessToken())
            .build()
            .toUriString();

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
