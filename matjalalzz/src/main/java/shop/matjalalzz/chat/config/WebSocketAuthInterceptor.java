package shop.matjalalzz.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.app.TokenProvider;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.oauth2.mapper.OAuth2Mapper;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor.getFirstNativeHeader("Authorization"));
            if (tokenProvider.validate(token)) {
                TokenBodyDto tokenBodyDto = tokenProvider.parseAccessToken(token);
                PrincipalUser principalUser = OAuth2Mapper.toPrincipalUser(tokenBodyDto);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principalUser, token, principalUser.getAuthorities()
                );

                accessor.setUser(authentication);
                accessor.getSessionAttributes().put("userId", principalUser.getId());
                return message;

            } else {
                throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
            }
        }
        return message;
    }

    private String resolveToken(String bearerToken) {

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
