package shop.matjalalzz.user.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import shop.matjalalzz.user.app.UserFacade;
import shop.matjalalzz.user.dto.LoginRequest;
import shop.matjalalzz.user.dto.OAuthSignUpRequest;
import shop.matjalalzz.user.dto.SignUpRequest;

@Tag(name = "사용자 API", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserFacade userFacade;

    @Operation(summary = "회원가입", description = "폼 로그인 회원가입(Completed)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public BaseResponse<Void> signup(@RequestBody @Valid SignUpRequest signUpRequestDto) {
        userFacade.signup(signUpRequestDto);
        return BaseResponse.ok(BaseStatus.CREATED); //201
    }

    @Operation(summary = "회원가입", description = "OAuth 추가 회원가입(Completed)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup/oauth")
    public BaseResponse<Void> oauthSignup(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @RequestBody @Valid OAuthSignUpRequest oauthSignUpRequestDto,
        HttpServletResponse response) {
        userFacade.oauthSignup(userInfo.getId(), oauthSignUpRequestDto, response);
        return BaseResponse.ok(BaseStatus.CREATED); //201
    }

    @Operation(summary = "회원탈퇴", description = "회원 탈퇴 기능(Inprogress)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/delete")
    public void deleteUser(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @Parameter(hidden = true)
        @CookieValue(name = "refreshToken") String refreshToken,
        HttpServletResponse response) {
        userFacade.deleteUser(userInfo.getId(), refreshToken, response);
    }

    @Operation(
        summary = "Form 로그인",
        description = "폼 로그인으로 로그인합니다. 성공 시 액세스 토큰은 헤더, 리프레시 토큰은 쿠키에 포함됩니다.(Completed)"
    )
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    public BaseResponse<Void> login(@RequestBody @Valid LoginRequest loginRequest,
        HttpServletResponse response) {
        userFacade.login(loginRequest, response);
        return BaseResponse.ok(BaseStatus.OK); //200
    }
}
