package shop.matjalalzz.global.security.oauth2.app;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.OAuth2Exception;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.oauth2.dto.GoogleResponseDto;
import shop.matjalalzz.global.security.oauth2.dto.KakaoResponseDto;
import shop.matjalalzz.global.security.oauth2.dto.NaverResponseDto;
import shop.matjalalzz.global.security.oauth2.dto.OAuth2ResponseDto;
import shop.matjalalzz.global.security.oauth2.mapper.OAuth2Mapper;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public PrincipalUser loadUser(OAuth2UserRequest userRequest) {
        // 기본 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2ResponseDto oAuth2ResponseDto = null;

        switch (provider) {
            case "google":
                oAuth2ResponseDto = new GoogleResponseDto(oAuth2User.getAttributes());
                break;
            case "naver":
                oAuth2ResponseDto = new NaverResponseDto(oAuth2User.getAttributes());
                break;
            case "kakao":
                oAuth2ResponseDto = new KakaoResponseDto(oAuth2User.getAttributes());
                break;
            default:
                throw new BusinessException(ErrorCode.INVALID_PROVIDER);
        }

        // providerId로 사용자 찾기
        String oauthId = oAuth2ResponseDto.getProvider() + "_" + oAuth2ResponseDto.getProviderId();
        Optional<User> found = userRepository.findByOauthId(oauthId);
        if (found.isPresent()) {
            return OAuth2Mapper.toPrincipalUser(found.get());
        }

        String email = oAuth2ResponseDto.getEmail();
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new OAuth2Exception(ErrorCode.EMAIL_ALREADY_EXISTS);
        });

        User newUser = userRepository.save(OAuth2Mapper.toUser(oAuth2ResponseDto));

        return OAuth2Mapper.toPrincipalUser(newUser);
    }

}
