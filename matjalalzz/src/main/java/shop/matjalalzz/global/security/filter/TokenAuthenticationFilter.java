package shop.matjalalzz.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.app.TokenProvider;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.oauth2.mapper.OAuth2Mapper;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response
        , FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null || token.isBlank()) {
            // 인증 정보가 없으면 필터 통과
            filterChain.doFilter(request, response);
            return;
        }

        if (tokenProvider.validate(token)) {
            TokenBodyDto tokenBodyDto = tokenProvider.parseAccessToken(token);
            PrincipalUser principalUser = OAuth2Mapper.toPrincipalUser(tokenBodyDto);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalUser, token, principalUser.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            response.setStatus(ErrorCode.INVALID_ACCESS_TOKEN.getStatus().value());
            response.getWriter().write(ErrorCode.INVALID_ACCESS_TOKEN.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
