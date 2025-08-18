package shop.matjalalzz.user.dto;

import shop.matjalalzz.user.entity.enums.Role;

public interface LoginInfoView {
    Long getUserId();
    String getPassword();
    Role getRole();
    String getEmail();
    String getRefreshToken();
}
