package shop.matjalalzz.global.security.jwt.dto;

import lombok.Builder;

@Builder
public record LoginTokenResponseDto(
    String accessToken,
    String refreshToken) {

}
