package shop.matjalalzz.user.app;


import static shop.matjalalzz.global.exception.domain.ErrorCode.EMAIL_ALREADY_EXISTS;
import static shop.matjalalzz.global.exception.domain.ErrorCode.INVALID_REFRESH_TOKEN;
import static shop.matjalalzz.global.exception.domain.ErrorCode.USER_NOT_FOUND;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.jwt.app.TokenProvider;
import shop.matjalalzz.global.security.jwt.dao.RefreshTokenRepository;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.global.security.jwt.mapper.TokenMapper;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.dto.LoginRequest;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;
import shop.matjalalzz.user.dto.OAuthSignUpRequest;
import shop.matjalalzz.user.dto.SignUpRequest;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.mapper.UserMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${custom.jwt.token-validity-time.refresh}")
    private int refreshTokenValiditySeconds;

    @Transactional
    public void login(LoginRequest dto, HttpServletResponse response) {
        //가입된 email과 password가 같은지 확인
        User found = userRepository.findByEmail(dto.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), found.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);  //404
        }

        String accessToken = tokenProvider.issueAccessToken(
            found.getId(), found.getRole(), found.getEmail()
        );

        RefreshToken refreshToken = refreshTokenRepository.findByUser(found)
            .orElseGet(() -> refreshTokenRepository.save(
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
        Cookie cookie = new Cookie("refreshToken", refreshToken.getRefreshToken());
        cookie.setHttpOnly(true);
        //cookie.setSecure(true); // HTTPS 환경
        cookie.setSecure(false); // HTTP 환경
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenValiditySeconds); // 7일
        response.addCookie(cookie);
    }

    //암호화 후 db에 회원가입 정보 저장
    //BaseResponse로 지정한 내용에 http 상태 코드를 수정 후 다시 ResponseEntity로 감싸서 보냄
    @Transactional
    public void signup(SignUpRequest dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);  //409
        }

        User user = UserMapper.toUser(dto, passwordEncoder);
//        user.setCreatedBy(user.getName());
//        user.setUpdatedBy(user.getName()); 이거를 써도 회원가입 형태는 anonymous가 뜨는 문제 발생
        userRepository.save(user);

    }

    @Transactional
    public void oauthSignup(String email, OAuthSignUpRequest dto) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);  //409
        }

        User user = UserMapper.toOAuthUser(dto);
//        user.setCreatedBy(user.getName());
//        user.setUpdatedBy(user.getName()); 이거를 써도 회원가입 형태는 anonymous가 뜨는 문제 발생
        userRepository.save(user);

    }

    @Transactional
    public void deleteUser(Long userId, String refreshToken,
        HttpServletResponse response) {
        User tokenUser = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        //refresh token 비교
        RefreshToken foundRefreshToken = refreshTokenRepository.findByUser(tokenUser)
            .orElseThrow(() -> new BusinessException(INVALID_REFRESH_TOKEN));

        if (!foundRefreshToken.getRefreshToken().equals(refreshToken)) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);  //401
        }

        refreshTokenRepository.delete(foundRefreshToken);

        userRepository.delete(tokenUser);

        // 쿠키 삭제 처리
        Cookie cookie = new Cookie("refreshToken", null); // 이름 동일, 값은 null
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        //cookie.setSecure(true); // HTTPS 환경
        cookie.setSecure(false); // HTTP 환경
        cookie.setMaxAge(0); // 즉시 만료
        //cookie.setDomain("your-domain.com"); // 필요 시 설정
        response.addCookie(cookie);
    }

    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        return UserMapper.toMyInfoResponse(user);
    }

    @Transactional
    public void updateMyInfo(Long userId, MyInfoUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        UserMapper.update(user, request);
    }
}