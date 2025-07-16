package shop.matjalalzz.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.user.entity.enums.Role;

public class WithCustomMockUserSecurityContextFactory
    implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        String username = annotation.username();
        Role role = annotation.role();
        long id = annotation.id();

        PrincipalUser principalUser = PrincipalUser.builder()
            .email(username)
            .role(role)
            .id(id)
            .build();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            principalUser, null, principalUser.getAuthorities());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
        return context;
    }
}
