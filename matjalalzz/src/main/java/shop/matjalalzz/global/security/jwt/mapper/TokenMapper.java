package shop.matjalalzz.global.security.jwt.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.global.security.jwt.dto.LoginTokenResponse;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.user.entity.enums.Role;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
