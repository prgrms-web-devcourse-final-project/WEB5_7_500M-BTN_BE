package shop.matjalalzz.chat.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import shop.matjalalzz.chat.dto.StompPrincipal;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.PrincipalUser;

@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        if (accessor.getCommand().equals(StompCommand.CONNECT)) {
            PrincipalUser user = (PrincipalUser) accessor.getSessionAttributes().get("principal");
            if (user == null) {
                throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
            }
            StompPrincipal principal = StompPrincipal.builder()
                .userId(user.getId())
                .principalUser(user)
                .build();
            accessor.setUser(principal);
        }
        return message;
    }
}
