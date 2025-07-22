package shop.matjalalzz.chat.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import shop.matjalalzz.chat.app.ChatService;
import shop.matjalalzz.chat.dto.ChatJoinRequest;
import shop.matjalalzz.chat.dto.ChatLoadRequest;
import shop.matjalalzz.chat.dto.ChatMessageRequest;
import shop.matjalalzz.chat.dto.ChatMessageResponse;
import shop.matjalalzz.chat.dto.StompPrincipal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest message,
        StompPrincipal user) {
        log.info("Sending message: " + message);

        ChatMessageResponse messageResponse = chatService.sendMessage(message, user.getId());
        messagingTemplate.convertAndSend("/topic/party/" + message.partyId(), messageResponse);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatJoinRequest request,
        StompPrincipal user) {
        log.info("Adding user: {} Into {}", user.getId(), request.partyId());

        ChatMessageResponse messageResponse = chatService.join(request, user.getId());

        messagingTemplate.convertAndSend("/topic/party/" + request.partyId(), messageResponse);
    }

    @MessageMapping("/chat/load")
    public void loadChatHistory(@Payload ChatLoadRequest chatLoadRequest,
        StompHeaderAccessor accessor) {
        log.info("Loading chat history for request: " + chatLoadRequest);

        List<ChatMessageResponse> chatMessageRequests = chatService.loadMessages(chatLoadRequest);

        messagingTemplate.convertAndSendToUser(accessor.getSessionId(), "/queue/load",
            chatMessageRequests);
    }
}
