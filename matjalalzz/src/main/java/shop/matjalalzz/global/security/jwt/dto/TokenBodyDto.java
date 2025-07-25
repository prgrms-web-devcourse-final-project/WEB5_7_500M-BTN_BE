package shop.matjalalzz.global.security.jwt.dto;

import lombok.Builder;
import shop.matjalalzz.user.entity.enums.Role;

@Builder
public record TokenBodyDto(Long userId, String email, Role role) {

}
