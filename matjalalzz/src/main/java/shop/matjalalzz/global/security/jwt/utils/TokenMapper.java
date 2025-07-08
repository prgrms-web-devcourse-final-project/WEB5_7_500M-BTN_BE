package shop.matjalalzz.global.security.jwt.utils;

import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponseDto;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.jwt.entity.RefreshToken;
import shop.matjalalzz.user.domain.User;
import shop.matjalalzz.user.domain.enums.Role;

public class TokenMapper {
    public static RefreshToken toRefreshToken(String refreshToken, User user) {
        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .user(user)
                .build();
    }

    public static TokenBodyDto toTokenBodyDto(Long userId, String email, Role role) {
        return TokenBodyDto.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }

    public static LoginTokenResponseDto toLoginTokenResponseDto(
            String accessToken, String refreshToken) {
        return LoginTokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
