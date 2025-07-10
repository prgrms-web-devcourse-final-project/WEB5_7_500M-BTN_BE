package shop.matjalalzz.user.api;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.dto.LoginRequest;
import shop.matjalalzz.user.dto.OAuthSignUpRequest;
import shop.matjalalzz.user.dto.SignUpRequest;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "폼 로그인 회원가입")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public BaseResponse<Void> signup(@RequestBody @Valid SignUpRequest signUpRequestDto) {
        userService.signup(signUpRequestDto);
        return BaseResponse.ok(BaseStatus.CREATED);//201
    }

    @Operation(summary = "회원가입", description = "OAuth 추가 회원가입")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup/oauth")
    public BaseResponse<Void> oauthSignup(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @RequestBody @Valid OAuthSignUpRequest oauthSignUpRequestDto) {
        userService.oauthSignup(userInfo.getEmail(), oauthSignUpRequestDto);
        return BaseResponse.ok(BaseStatus.CREATED); //201
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/delete")
    public void deleteUser(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @CookieValue(name = "refreshToken") String refreshToken,
        HttpServletResponse response) {
        userService.deleteUser(userInfo.getId(), refreshToken, response);
    }

    @ResponseStatus(HttpStatus.OK)
    //반환으로 헤더에 토큰값을 넣어줘야 하니깐 HttpServletResponse
    @PostMapping("/login")
    public BaseResponse<Void> login(@RequestBody @Valid LoginRequest loginRequest,
        HttpServletResponse response) {
        userService.login(loginRequest, response);
        return BaseResponse.ok(BaseStatus.OK); //200
    }
}
