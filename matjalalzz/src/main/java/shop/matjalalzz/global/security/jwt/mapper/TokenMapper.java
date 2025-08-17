package shop.matjalalzz.global.security.jwt.mapper;

import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponse;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

public class TokenMapper {

    public static TokenBodyDto toTokenBodyDto(Long userId, String email, Role role) {
        return TokenBodyDto.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }

    public static LoginTokenResponse toLoginTokenResponseDto(
            String accessToken, String refreshToken) {
        return LoginTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
