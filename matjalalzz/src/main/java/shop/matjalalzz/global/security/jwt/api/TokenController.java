package shop.matjalalzz.global.security.jwt.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.app.TokenService;
import shop.matjalalzz.global.security.jwt.dto.AccessTokenResponseDto;

@Tag(name = "사용자 API", description = "사용자 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @Value("${custom.jwt.redirect-login-success}")
    private String redirectSuccess;

    @Operation(
        summary = "액세스 토큰 재발급",
        description = "쿠키의 리프레시 토큰을 이용해 새로운 액세스 토큰을 재발급합니다."
    )
    @PostMapping("/reissue-token")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<AccessTokenResponseDto> refreshToken(
        @Parameter(hidden = true)
        @CookieValue(name = "refreshToken") String refreshToken) {
        return BaseResponse.ok(tokenService.refreshAccessToken(refreshToken), BaseStatus.OK);
    }

    @Operation(
        summary = "로그아웃",
        description = "쿠키의 리프레시 토큰을 무효화하고 로그아웃 처리합니다."
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> logout(
        @AuthenticationPrincipal PrincipalUser userInfo, HttpServletResponse response) {
        tokenService.logout(userInfo.getId(), response);

        return BaseResponse.ok(BaseStatus.OK);
    }

    @Hidden
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/set-cookie")
    public ResponseEntity<Void> setCookieAndRedirect(
        @RequestParam("accessToken") String accessToken,
        HttpServletResponse response) {

        tokenService.setCookie(accessToken, response);
        HttpHeaders h = new HttpHeaders();
        h.setLocation(URI.create(redirectSuccess + "?accessToken=" + accessToken));
        return new ResponseEntity<>(h, HttpStatus.FOUND);
    }

    @Operation(
        summary = "OAuth2 로그인 진입점 URL 안내",
        description = "프론트는 이 URL로 리디렉션하여 OAuth2 로그인을 시작합니다. 예: /oauth2/authorization/google"
    )
    @GetMapping("/authorization-info")
    public Map<String, String> oauth2Urls() {
        Map<String, String> map = new HashMap<>();
        map.put("google", "/oauth2/authorization/google");
        map.put("kakao", "/oauth2/authorization/kakao");
        map.put("naver", "/oauth2/authorization/naver");
        return map;
    }
}
