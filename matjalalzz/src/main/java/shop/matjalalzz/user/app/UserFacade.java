package shop.matjalalzz.user.app;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.security.jwt.app.TokenProvider;
import shop.matjalalzz.global.security.jwt.app.TokenService;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.global.security.jwt.mapper.TokenMapper;
import shop.matjalalzz.global.util.CookieUtils;
import shop.matjalalzz.party.app.PartyFacade;
import shop.matjalalzz.reservation.app.ReservationFacade;
import shop.matjalalzz.user.dto.LoginRequest;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;
import shop.matjalalzz.user.dto.OAuthSignUpRequest;
import shop.matjalalzz.user.dto.SignUpRequest;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;
import shop.matjalalzz.user.mapper.UserMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final PreSignedProvider preSignedProvider;
    private final TokenService tokenService;
    private final PartyFacade partyFacade;
    private final ReservationFacade reservationFacade;

    @Value("${custom.jwt.token-validity-time.refresh}")
    private int refreshTokenValiditySeconds;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String baseUrl;

    @Transactional
    public void login(LoginRequest dto, HttpServletResponse response) {
        //가입된 email과 password가 같은지 확인
        User found = userService.getUserByEmail(dto.email());

        if (!passwordEncoder.matches(dto.password(), found.getPassword()) || found.isDeleted()) {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);  //404
        }

        String accessToken = tokenProvider.issueAccessToken(
            found.getId(), found.getRole(), found.getEmail()
        );

        RefreshToken refreshToken = tokenService.findRefreshToken(found)
            .orElseGet(() -> tokenService.saveRefreshToken(
                TokenMapper.toRefreshToken(
                    tokenProvider.issueRefreshToken(found.getId()), found
                )
            ));

        if (!tokenProvider.validate(refreshToken.getRefreshToken())) {
            String reissueRefreshToken = tokenProvider.issueRefreshToken(found.getId());
            refreshToken.updateRefreshToken(reissueRefreshToken);
        }

        // http only 쿠키 방식으로 refresh Token을 클라이언트에게 줌
        response.setHeader("Authorization", "Bearer " + accessToken);
        CookieUtils.setRefreshTokenCookie(response, refreshToken.getRefreshToken(),
            refreshTokenValiditySeconds);
    }

    @Transactional
    public void signup(SignUpRequest dto) {
        Optional<User> user = userService.findUserByEmail(dto.email());

        if (user.isPresent()) {
            if(user.get().isDeleted()) {
                user.get().recover();
                return;
            }

            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);  //409
        }

        String encodedPassword = passwordEncoder.encode(dto.password());

        User newUser = UserMapper.toUser(dto, encodedPassword, Role.USER);

        userService.saveUser(newUser);

        log.info("회원가입 성공");
    }

    @Transactional
    public void oauthSignup(long userId, OAuthSignUpRequest request, HttpServletResponse response) {
        User user = userService.getUserById(userId);

        if(user.isDeleted()) {
            user.recover();
            return;
        }

        user.oauthSignup(request);

        String accessToken = tokenProvider.issueAccessToken(
            user.getId(), user.getRole(), user.getEmail()
        );

        log.info("회원가입 성공");

        response.setHeader("Authorization", "Bearer " + accessToken);
    }

    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long userId) {
        User user = userService.getUserById(userId);

        String profile = user.getProfileKey() == null ? null : baseUrl + user.getProfileKey();

        return UserMapper.toMyInfoResponse(user, profile);
    }

    @Transactional
    public void updateMyInfo(Long userId, MyInfoUpdateRequest request) {
        User user = userService.getUserById(userId);

        String key = StringUtils.removeStart(request.profileKey(), baseUrl);

        if (!key.equals(user.getProfileKey())) {
            preSignedProvider.deleteObject(user.getProfileKey());
        }

        user.update(request);
    }

    @Transactional
    public void deleteUser(Long userId, String refreshToken,
        HttpServletResponse response) {
        User user = userService.getUserById(userId);

        //refresh token 비교
        RefreshToken foundRefreshToken = tokenService.findRefreshToken(user)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!foundRefreshToken.getRefreshToken().equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);  //401
        }

        if(user.getRole() == Role.OWNER) {
            throw new BusinessException(ErrorCode.OWNER_CANNOT_WITHDRAW);
        }

        partyFacade.deletePartyForWithdraw(user);
        partyFacade.quitPartyForWithdraw(user);

        reservationFacade.cancelReservationForWithdraw(user);

        tokenService.deleteRefreshToken(foundRefreshToken);

        user.delete();

        CookieUtils.deleteRefreshTokenCookie(response);
    }
}
