package shop.matjalalzz.global.security.jwt.api;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.matjalalzz.global.security.jwt.app.TokenService;
import shop.matjalalzz.global.security.jwt.dto.AccessTokenResponseDto;
import shop.matjalalzz.global.security.jwt.dto.RefreshTokenRequestDto;
import shop.matjalalzz.global.unit.BaseResponse;
import shop.matjalalzz.user.dto.LoginRequest;

@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDto> refreshToken(
            @CookieValue(name = "refreshToken") String refreshToken) {
        return ResponseEntity.ok(tokenService.refreshAccessToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse response) {
        tokenService.logout(refreshToken, response);
        return ResponseEntity.ok().build();
    }
}
