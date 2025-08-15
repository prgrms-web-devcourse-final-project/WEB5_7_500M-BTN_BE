package shop.matjalalzz.global.security.jwt.dto;

import shop.matjalalzz.user.entity.enums.Role;

public record AuthUserInfoDto(Role role, String email) {

}
