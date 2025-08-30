package shop.matjalalzz.global.security.jwt.dto.projection;

import shop.matjalalzz.user.entity.enums.Role;

public interface AuthUserProjection {
    String getRefreshToken();
    Role getRole();
    String getEmail();
}
