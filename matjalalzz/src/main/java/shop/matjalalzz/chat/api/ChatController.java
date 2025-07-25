package shop.matjalalzz.chat.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import shop.matjalalzz.chat.app.ChatService;
import shop.matjalalzz.chat.dto.ChatMessagePageResponse;
import shop.matjalalzz.chat.dto.ChatMessageRequest;
import shop.matjalalzz.chat.dto.ChatMessageResponse;
import shop.matjalalzz.chat.dto.StompPrincipal;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.exception.dto.ErrorResponse;
import shop.matjalalzz.global.security.PrincipalUser;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest message,
        StompPrincipal user) {
        log.trace("Sending message: {}", message);

        ChatMessageResponse messageResponse = chatService.sendMessage(message, user.getId());
        messagingTemplate.convertAndSend("/topic/party/" + message.partyId(), messageResponse);
    }

    @GetMapping("/parties/{partyId}/chat/restore")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<List<ChatMessageResponse>> restoreChat(
        @PathVariable Long partyId,
        @RequestParam Long cursor,
        @AuthenticationPrincipal PrincipalUser user) {

        List<ChatMessageResponse> chatMessages = chatService.restoreMessages(partyId, cursor,
            user.getId());

        return BaseResponse.ok(chatMessages, BaseStatus.OK);
    }

    @GetMapping("/parties/{partyId}/chat/load")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ChatMessagePageResponse> loadChatHistory(@PathVariable Long partyId,
        @RequestParam Long cursor,
        @AuthenticationPrincipal PrincipalUser user) {

        ChatMessagePageResponse chatMessages = chatService.loadMessages(partyId, cursor,
            user.getId());

        return BaseResponse.ok(chatMessages, BaseStatus.OK);

    }

    @MessageExceptionHandler(BusinessException.class)
    public void handleException(StompHeaderAccessor accessor, BusinessException ex) {
        ErrorCode code = ex.getErrorCode();

        log.debug("WebSocket Business Exception occurred - Code: {}, Message: {}, Session: {}",
            code.name(), code.getMessage(), accessor.getSessionId());

        ErrorResponse response = ErrorResponse.builder()
            .status(code.getStatus().value())
            .code(code.name())
            .message(code.getMessage())
            .path("/ws/chat")
            .build();

        messagingTemplate.convertAndSend("/queue/error-" + accessor.getSessionId(), response);
    }
}
