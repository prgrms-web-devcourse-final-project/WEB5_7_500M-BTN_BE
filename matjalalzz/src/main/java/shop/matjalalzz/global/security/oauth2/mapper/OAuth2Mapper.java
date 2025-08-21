package shop.matjalalzz.global.security.oauth2.mapper;

import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.oauth2.dto.OAuth2Response;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

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

    public static User toUser(OAuth2Response oAuth2Response) {
        String oauthId = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();
        String name = oAuth2Response.getName();

        return User.builder()
            .oauthId(oauthId)
            .email(email)
            .nickname(name)
            .name(name)
            .role(Role.TMP)
            .build();
    }
}
