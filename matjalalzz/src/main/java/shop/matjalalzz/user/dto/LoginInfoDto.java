package shop.matjalalzz.user.dto;

import shop.matjalalzz.user.entity.enums.Role;

public record LoginInfoDto(
    Long userId, String password, Role role, String email, String refreshToken) {

}
