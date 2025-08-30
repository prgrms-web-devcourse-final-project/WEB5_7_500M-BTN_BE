package shop.matjalalzz.global.security.jwt.dto;

import lombok.Builder;

@Builder
public record LoginTokenResponse(
    String accessToken,
    String refreshToken) {

}
