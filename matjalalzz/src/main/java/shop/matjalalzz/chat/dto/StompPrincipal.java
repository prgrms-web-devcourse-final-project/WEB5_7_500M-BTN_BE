package shop.matjalalzz.chat.dto;

import java.security.Principal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.matjalalzz.global.security.PrincipalUser;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StompPrincipal implements Principal {

    private Long userId;
    private PrincipalUser principalUser;

    public Long getId() {
        return userId;
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}
