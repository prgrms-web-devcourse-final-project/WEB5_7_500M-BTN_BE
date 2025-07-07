package shop.matjalalzz.user.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.unit.BaseResponse;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.dto.DeleteUserRequest;
import shop.matjalalzz.user.dto.LoginRequest;
import shop.matjalalzz.user.dto.SingUpRequest;


@RestController
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "폼 로그인 회원가입")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/user/signup")
    public BaseResponse<Void> signup(@RequestBody @Valid SingUpRequest signUpRequestDto) {
        userService.signup(signUpRequestDto);
        return BaseResponse.okOnlyStatus(HttpStatus.CREATED);//201
    }

    @ResponseStatus(HttpStatus.OK)
    //반환으로 헤더에 토큰값을 넣어줘야 하니깐 HttpServletResponse
    @PostMapping("/user/login")
    public BaseResponse<Void> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        userService.login(loginRequest, response);
        return BaseResponse.okOnlyStatus(HttpStatus.OK); //200
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/user/logout")
    public BaseResponse<Void> logout( @Parameter(hidden = true) @RequestHeader("Authorization") @NotBlank String accessToken,HttpServletRequest request,HttpServletResponse response) {
        userService.logout(accessToken, request, response);
        return BaseResponse.okOnlyStatus(HttpStatus.OK); //200
    }

    //프론트에서 access token 만료로 인해 재발급을 요청함 (본인이 가진 refresh token을 가지고 요청합니다)
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/user/reissue-token")
    public BaseResponse<Void> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        userService.reissue(request, response);
        return BaseResponse.okOnlyStatus(HttpStatus.OK); //200
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/user/delete")
    public void deleteUser( @Parameter(hidden = true) @RequestHeader("Authorization") @NotBlank String accessToken,
        @RequestBody @Valid DeleteUserRequest deleteUserRequest, HttpServletRequest request, HttpServletResponse response) {
        userService.deleteUser(accessToken, deleteUserRequest, request, response);
    }

}
