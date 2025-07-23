package shop.matjalalzz.chat.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompSessionInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            // /user/queue/load 구독 시 세션별 고유 큐로 변환
            if ("/user/queue/load".equals(destination)) {
                String sessionId = accessor.getSessionId();
                accessor.setDestination("/queue/load-" + sessionId);
            } else if ("/user/queue/error".equals(destination)) {
                String sessionId = accessor.getSessionId();
                accessor.setDestination("/queue/error-" + sessionId);
            }
        }

        return message;
    }
}
