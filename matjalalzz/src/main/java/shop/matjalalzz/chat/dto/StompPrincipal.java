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

    private String sessionId;
    private PrincipalUser principalUser;

    public Long getId() {
        return principalUser.getId();
    }

    @Override
    public String getName() {
        return sessionId;
    }
}
