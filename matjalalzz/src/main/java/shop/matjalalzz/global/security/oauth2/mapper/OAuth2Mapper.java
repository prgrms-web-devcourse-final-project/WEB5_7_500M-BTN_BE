package shop.matjalalzz.global.security.oauth2.mapper;

import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.oauth2.dto.OAuth2ResponseDto;
import shop.matjalalzz.user.entity.User;

public class OAuth2Mapper {
    public static PrincipalUser toPrincipalUser(User user) {
        return PrincipalUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public static PrincipalUser toPrincipalUser(TokenBodyDto tokenBodyDto) {
        return PrincipalUser.builder()
                .id(tokenBodyDto.userId())
                .email(tokenBodyDto.email())
                .role(tokenBodyDto.role())
                .build();
    }

    public static User toUser(OAuth2ResponseDto oAuth2ResponseDto) {
        String oauthId = oAuth2ResponseDto.getProvider() + "_" + oAuth2ResponseDto.getProviderId();
        String email = oAuth2ResponseDto.getEmail();
        String name = oAuth2ResponseDto.getName();

        return User.builder()
                .oauthId(oauthId)
                .email(email)
                .nickname(name)
                .build();
    }
}
