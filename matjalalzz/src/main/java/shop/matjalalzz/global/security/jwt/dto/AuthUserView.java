package shop.matjalalzz.global.security.jwt.dto;

import shop.matjalalzz.user.entity.enums.Role;

public interface AuthUserView {
    String getRefreshToken();
    Role getRole();
    String getEmail();
}
