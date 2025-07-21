package shop.matjalalzz.chat.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import shop.matjalalzz.chat.app.ChatService;
import shop.matjalalzz.chat.dto.ChatMessageDto;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto message) {
        log.info("Sending message: " + message);
        ChatMessageDto sendMessage = chatService.save(message);
        messagingTemplate.convertAndSend("/topic/party/" + message.partyId(), sendMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("Adding user: " + message);
        headerAccessor.getSessionAttributes().put("username", message.sender());
        headerAccessor.getSessionAttributes().put("roomId", message.partyId());

        messagingTemplate.convertAndSend("/topic/party/" + message.partyId(), message);
    }

//    @MessageMapping("/chat/load")
//    public void loadChatHistory(@Payload ChatLoadRequest chatLoadRequest,
//        SimpMessageHeaderAccessor headerAccessor) {
//        log.info("Loading chat history for request: " + chatLoadRequest);
//
//        List<ChatMessageDto> chatMessageDtos = chatService.loadMessages(chatLoadRequest);
//
//        // 세션 ID 가져오기 (여러 방법 시도)
//        String sessionId = null;
//
//        // 방법 1: Principal에서 가져오기
//        if (headerAccessor.getUser() != null) {
//            sessionId = headerAccessor.getUser().getName();
//        }
//
//        if (sessionId != null) {
//            log.info("Sending {} messages to session: {}", chatMessageDtos.size(), sessionId);
//
//            messagingTemplate.convertAndSendToUser(sessionId, "/queue/load", chatMessageDtos);
//        } else {
//            log.warn("Session ID is null, cannot send chat history");
//        }
//    }
}
