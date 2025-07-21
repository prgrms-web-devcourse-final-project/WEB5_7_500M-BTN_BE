package shop.matjalalzz.chat.config;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import shop.matjalalzz.chat.dto.ChatMessageDto;
import shop.matjalalzz.chat.entity.MessageType;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("Connected to websocket");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headerAccessor.getUser();
        if (user != null) {
            log.info("Connect User: {}", user.getName());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("Disconnected from websocket");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        Long partyId = (Long) headerAccessor.getSessionAttributes().get("partyId");

        if (username != null && partyId != null) {
            log.info("Disconnect Username {} and roomId {}", username, partyId);
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                .type(MessageType.LEAVE)
                .partyId(partyId)
                .sender(username)
                .build();

            messagingTemplate.convertAndSend("/topic/public/" + partyId, chatMessageDto);
        }
    }

}
