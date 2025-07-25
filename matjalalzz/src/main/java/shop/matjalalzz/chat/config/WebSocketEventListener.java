package shop.matjalalzz.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import shop.matjalalzz.chat.dto.StompPrincipal;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("Connected to websocket");
        StompPrincipal principalUser = (StompPrincipal) event.getUser();

        if (principalUser != null) {
            log.info("Connect User: {}", principalUser.getId());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("Disconnected from websocket");
        StompPrincipal principalUser = (StompPrincipal) event.getUser();

        if (principalUser != null) {
            log.info("Disconnect User: {}", principalUser.getId());
        }
    }
}
