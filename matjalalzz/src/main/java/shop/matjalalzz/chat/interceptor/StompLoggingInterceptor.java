package shop.matjalalzz.chat.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompLoggingInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor != null && accessor.getCommand() != null) {
            log.debug("[{}] -> {} | SessionId: {}",
                accessor.getCommand(),
                accessor.getDestination(),
                accessor.getSessionId());

            if (accessor.getCommand() == StompCommand.SEND) {
                log.debug("Message payload: {}", new String((byte[]) message.getPayload()));
            }
        }

        return message;
    }
}

