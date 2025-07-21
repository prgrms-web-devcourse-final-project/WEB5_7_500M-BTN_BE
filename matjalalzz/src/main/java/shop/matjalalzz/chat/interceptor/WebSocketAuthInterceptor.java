package shop.matjalalzz.chat.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.PrincipalUser;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        if (accessor.getCommand().equals(StompCommand.CONNECT)) {
            PrincipalUser user = (PrincipalUser) accessor.getSessionAttributes().get("user");
            if (user == null) {
                throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
            }
            Authentication authentication = new PreAuthenticatedAuthenticationToken(
                user, null, user.getAuthorities()
            );
            //TODO: PrincipalUser가 Principal을 구현해야 하는가?
            accessor.setUser(authentication);
        }
        return message;
    }
}
