package shop.matjalalzz.user.dto.projection;

import shop.matjalalzz.user.entity.enums.Role;

public interface LoginUserProjection {
    Long getUserId();
    String getPassword();
    Role getRole();
    String getEmail();
    String getRefreshToken();
}
