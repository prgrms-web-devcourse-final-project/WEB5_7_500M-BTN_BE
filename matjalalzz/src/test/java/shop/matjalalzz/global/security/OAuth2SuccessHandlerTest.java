package shop.matjalalzz.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import shop.matjalalzz.global.security.handler.OAuth2SuccessHandler;
import shop.matjalalzz.global.security.jwt.app.TokenService;
import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponseDto;
import shop.matjalalzz.user.entity.enums.Role;

@SpringBootTest
@AutoConfigureMockMvc
class OAuth2SuccessHandlerTest {

    @Autowired
    OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    TokenService tokenService;

    @Test
    @DisplayName("OAuth 로그인 성공 시 엑세스, 리프레시 토큰 발급 및 /auth/callback 으로 리다이렉트")
    void onAuthenticationSuccess() throws Exception {
        // given
        PrincipalUser principal = PrincipalUser.builder()
            .id(1L).email("test@example.com").role(Role.USER).build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities());

        MockHttpServletRequest req  = new MockHttpServletRequest();
        MockHttpServletResponse res  = new MockHttpServletResponse();

        LoginTokenResponseDto mockResponse = new LoginTokenResponseDto(
            "access.jwt", "refresh.jwt");

        when(tokenService.oauthLogin(principal.getEmail())).thenReturn(mockResponse);

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(req, res, auth);

        // then
        assertThat(res.getHeader("Authorization")).isEqualTo("Bearer access.jwt");

        Cookie cookie = res.getCookie("refreshToken");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getValue()).isEqualTo("refresh.jwt");
        assertThat(res.getRedirectedUrl()).contains("/auth/callback");
    }
}