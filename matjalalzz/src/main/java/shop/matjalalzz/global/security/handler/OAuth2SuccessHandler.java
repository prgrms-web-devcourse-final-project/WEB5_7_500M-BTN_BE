package shop.matjalalzz.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.app.TokenService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        PrincipalUser userInfo = (PrincipalUser) authentication.getPrincipal();

        String accessToken = tokenService.oauthLogin(userInfo.getUsername());

        String target = UriComponentsBuilder
            .fromPath("/users/set-cookie")
            .queryParam("accessToken", accessToken)
                .build(true)
                    .toUriString();
      
        response.sendRedirect(target);
    }
}
