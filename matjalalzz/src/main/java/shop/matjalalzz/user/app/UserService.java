package shop.matjalalzz.user.app;


import static shop.matjalalzz.global.exception.domain.ErrorCode.EMAIL_ALREADY_EXISTS;
import static shop.matjalalzz.global.exception.domain.ErrorCode.INVALID_REFRESH_TOKEN;
import static shop.matjalalzz.global.exception.domain.ErrorCode.USER_NOT_FOUND;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.user.adapter.UserDetail;
import shop.matjalalzz.user.domain.RefreshToken;
import shop.matjalalzz.user.domain.User;
import shop.matjalalzz.user.dto.Delete;
import shop.matjalalzz.user.dto.DeleteUserRequest;
import shop.matjalalzz.user.dto.Login;
import shop.matjalalzz.user.dto.LoginRequest;
import shop.matjalalzz.user.dto.SingUpRequest;
import shop.matjalalzz.user.mapper.UserMapper;
import shop.matjalalzz.user.dao.TokenRepository;
import shop.matjalalzz.user.dao.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    //private final BlackListRepository blackListRepository;
    private final UserContextService userContextService;
    private final StringRedisTemplate redisTemplate;



    //암호화 후 db에 회원가입 정보 저장
    //BaseResponse로 지정한 내용에 http 상태 코드를 수정 후 다시 ResponseEntity로 감싸서 보냄
    @Transactional
    public void signup(SingUpRequest dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);  //409
        }

        User user = UserMapper.toUser(dto, passwordEncoder);
//        user.setCreatedBy(user.getName());
//        user.setUpdatedBy(user.getName()); 이거를 써도 회원가입 형태는 anonymous가 뜨는 문제 발생
        userRepository.save(user);

    }

    //user id값으로 user 객체 반환
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    //가져온 객체가 없으면 에러, 있으면 user 반환
    public User getById(Long id) {
        return findById(id).orElseThrow(() -> {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);
        });
    }

    // user 객체를 UserDetail로 변환
    public UserDetail getDetails(Long id) {
        User findUser = getById(id);
        return UserDetail.UserDetailsMake(findUser);
    }

    //로그인
    @Transactional
    public void login(LoginRequest dto, HttpServletResponse response) {

        //가입된 email과 password가 같은지 확인
        Optional<User> findAdmin = userRepository.findByEmail(dto.email());

        if (findAdmin.isEmpty()) {                                 //이메일이 존재하지 않다 반환 시 찾을 때까지 이메일 무한 입력 가능성
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND); //404
        }

        User user = findAdmin.get();

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);  //404
        }

        //가입된 정보가 일치하고 db에 refresh token이 존재하고 있으면 기간이 만료된게 확인되면 다시 재발급
        String refreshToken;
        //null이든 뭐든 사용자 정보로 db에서 refresh token이 존재하는지 검색
        Optional<RefreshToken> optionalSavedToken = tokenRepository.findTop1ByUserIdOrderByIdDesc(user.getId());
        if (optionalSavedToken.isPresent()) {
            RefreshToken savedToken = optionalSavedToken.get();
            if (!jwtTokenProvider.validate(savedToken.getRefreshToken())) {
                //refresh token이 존재하지만 시간이 만료된 경우 발급 및 갱신
                refreshToken = jwtTokenProvider.issueRefreshToken(user.getId(), user.getRole(), user.getEmail());
                savedToken.newSetRefreshToken(refreshToken);
            } else {
                //refresh token이 존재하며 유효기간도 아직 유효한 경우 기존 토큰 재사용
                refreshToken = savedToken.getRefreshToken();
            }
        } else {
            //아예 토큰이 존재하지 않았던 경우로 새로 발급 및 저장
            refreshToken = jwtTokenProvider.issueRefreshToken(user.getId(), user.getRole(), user.getEmail());
            tokenRepository.save(new RefreshToken(refreshToken, user));
        }

        String accessToken = jwtTokenProvider.issueAccessToken(user.getId(), user.getRole(), user.getEmail());

        // 위에서 발급한 accessToken redis에 저장
        String key = "accessToken:" + user.getId();
        // 토큰의 만료 시각 - 현재 시각 = 남은 시간으로 TTL을 계산하는 방식
        long expiration = jwtTokenProvider.getExpiration(accessToken).getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, accessToken, expiration, TimeUnit.MILLISECONDS);


        // http only 쿠키 방식으로 refresh Token을 클라이언트에게 줌
        Login login = new Login(accessToken, refreshToken);
        response.setHeader("Authorization", "Bearer " + login.accessToken());
        Cookie cookie = new Cookie("refreshToken", login.refreshToken());
        cookie.setHttpOnly(true);
        //cookie.setSecure(true); // HTTPS 환경
        cookie.setSecure(false); // HTTP 환경
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(cookie);


    }

    // Access Token 만료 시 Refresh Token으로 Accesss Token을 재발급하는 코드
    @Transactional
    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = userContextService.extractRefreshTokenFromCookie(request);

        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }

        // 토큰 유효성 확인 및 정보 추출 (사용자에 대한 권한이 아닌 토큰에 대한 유효성만 검사를 하므로 밑 부분처럼 추가 검사들이 필요합니다.)
        if (!jwtTokenProvider.validate(refreshToken)) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);    //401
        }

        //요청을 한 사람이 기존에 회원가입이 되어 있는 사용자가 맞는지 검사
        Long userId = jwtTokenProvider.parseJwt(refreshToken).getUserId();
        if (userRepository.findById(userId).isEmpty()) {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);    //404 반환
        }
        //해당 user에 대한 정보가 있다면 User 객체로 가져옴
        User user = userRepository.findById(userId).get();

        // DB에 있는 RefreshToken과 일치 여부 확인
        //클라이언트가 서버로 refresh token을 보냈을 때, 이 토큰이 "서버에서 발급한 것이 맞는지" 검증
        if (tokenRepository.findTop1ByUserIdOrderByIdDesc(userId).isEmpty()) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);    //401 //401 반환
        }
        RefreshToken serverFindRefreshToken = tokenRepository.findTop1ByUserIdOrderByIdDesc(userId).get();

        //위에서 가져온 admin에 맞는 토큰 정보와 클라이언트가 요청으로 가져온 refresh Token이 같은지 다른지 확인해 위조 가능성을 체크
        if (!serverFindRefreshToken.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("RefreshToken 불일치 (위조 가능성!!!)");
        }


        //redis에서 accessToken 삭제
        String key = "accessToken:" + userId;
        redisTemplate.delete(key);


        //Refresh token이 유효하지만 access token 재발급 용도로 사용 후
        //Refresh Token이 노출되었을 수 있기 때문에, 사용 후에는 새로운 것으로 갱신하는 것이 안전하다
        String newAccessToken = jwtTokenProvider.issueAccessToken(user.getId(), user.getRole(), user.getEmail());

        //새로 발급한 accessToken으로 redis에 다시 저장
        String newKey = "accessToken:" + userId;
        long expiration = jwtTokenProvider.getExpiration(newAccessToken).getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(newKey, newAccessToken, expiration, TimeUnit.MILLISECONDS);


        String newRefreshToken = jwtTokenProvider.issueRefreshToken(user.getId(), user.getRole(), user.getEmail());

        serverFindRefreshToken.newSetRefreshToken(newRefreshToken); // 새로 토큰을 발급 받아 기존 refresh token을 갱신
        Login login = new Login(newAccessToken, newRefreshToken);// 클라이언트에게 보내줄 용도

        response.setHeader("Authorization", "Bearer " + login.accessToken());
        Cookie cookie = new Cookie("refreshToken", login.refreshToken());
        cookie.setHttpOnly(true);
        //cookie.setSecure(true); // HTTPS 환경
        cookie.setSecure(false); // HTTP 환경
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(cookie);
    }
    //로그아웃 = Redis에서 토큰 삭제
    // 만료 = Redis의 TTL로 자연스럽게 만료됨
    @Transactional
    public void logout(String accessToken, HttpServletRequest request, HttpServletResponse response) {
        //토큰 구조 먼저 확인
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        if (!jwtTokenProvider.validate(accessToken)) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);    //401
        }

        Long userId = jwtTokenProvider.parseJwt(accessToken).getUserId();

        Optional<RefreshToken> findrefreshToken = tokenRepository.findTop1ByUserIdOrderByIdDesc(userId);
        if (findrefreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);  // 401 반환
        }
        RefreshToken refreshToken = findrefreshToken.get();

        String refreshTokenFromCookie = userContextService.extractRefreshTokenFromCookie(request);

        if (!refreshToken.getRefreshToken().equals(refreshTokenFromCookie)) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);  // 401 반환
        }

        /// access Token을 블랙리스트 방식으로 사용 시 사용
        //토큰이 유효하다면, 이 토큰의 만료 시각을 가져온다. 블랙리스트에도 해당 만료 시간을 똑같이 넣어서 15분이면 15분 동안은 이 토큰을 사용하기 위해
        // Date expiration = jwtTokenProvider.getExpiration(
        //     accessToken); //만료 시간 추출해서 현재 시간이 만료가 예정된 시간보다 작으면 그 토큰을 사용하지 못하게
        // // blackListRepository.save(new BlackList(accessToken, expiration));
        tokenRepository.delete(refreshToken);

        //redis에서 accessToken 삭제
        String key = "accessToken:" + userId;
        redisTemplate.delete(key);

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

    @Transactional
    public void deleteUser(String accessToken, DeleteUserRequest deleteUserRequest
        ,HttpServletRequest request, HttpServletResponse response) {
        //토큰 구조 먼저 확인
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        Delete delete = UserMapper.toDelete(deleteUserRequest);

        // 1. 토큰에서 유저 정보 추출
        Long userId = jwtTokenProvider.parseJwt(accessToken).getUserId();

        // 2. DB에서 유저 조회
        User tokenUser = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        //이메일 비교
        if (!delete.email().equals(tokenUser.getEmail())) {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);  // 404
        }

        //refresh token 비교
        Optional<RefreshToken> tokenOptional = tokenRepository.findTop1ByUserIdOrderByIdDesc(userId);
        if (tokenOptional.isEmpty()) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);  //401
        }

        String refreshToken = userContextService.extractRefreshTokenFromCookie(request);
        if (!tokenOptional.get().getRefreshToken().equals(refreshToken)) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);  //401
        }

        //비밀번호 비교
        if (!passwordEncoder.matches(delete.password(), tokenUser.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_USER_NOT_FOUND);  //404
        }


        //refresh token DB에서 삭제
        List<RefreshToken> refreshTokens = tokenRepository.findAllByUserId(userId);
        tokenRepository.deleteAll(refreshTokens);

        //유저 삭제
        userRepository.findById(userId).ifPresent(userRepository::delete);

        //redis에서 accessToken 삭제
        String key = "accessToken:" + userId;
        redisTemplate.delete(key);

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
}