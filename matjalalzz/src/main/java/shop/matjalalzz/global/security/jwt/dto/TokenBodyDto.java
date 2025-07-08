package shop.matjalalzz.global.security.jwt.dto;

import lombok.Builder;
import shop.matjalalzz.user.domain.enums.Role;

@Builder
public record TokenBodyDto(Long userId, String email, Role role) {

}
