package shop.matjalalzz.global.util;

import java.security.Principal;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.security.PrincipalUser;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // 예시: Spring Security에서 현재 로그인한 사용자 이름 반환
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.of(authentication.getName()); // 또는 userId
    }

    // 로그인한 사용자의 UserId를 가져옴
    public Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof PrincipalUser)) {
            return Optional.empty();
        }

        PrincipalUser user = (PrincipalUser) principal;
        return Optional.of(user.getUserId());
    }

}