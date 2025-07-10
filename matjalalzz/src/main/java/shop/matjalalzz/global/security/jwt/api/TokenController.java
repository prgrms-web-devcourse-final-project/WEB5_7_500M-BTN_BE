package shop.matjalalzz.global.security.jwt.api;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.jwt.app.TokenService;
import shop.matjalalzz.global.security.jwt.dto.AccessTokenResponseDto;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/reissue-token")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<AccessTokenResponseDto> refreshToken(
        @CookieValue(name = "refreshToken") String refreshToken) {
        return BaseResponse.ok(tokenService.refreshAccessToken(refreshToken), BaseStatus.OK);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
        @CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {
        tokenService.logout(refreshToken, response);
    }
}
