package shop.matjalalzz.chat.api;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import shop.matjalalzz.chat.app.ChatCommandService;
import shop.matjalalzz.chat.app.ChatQueryService;
import shop.matjalalzz.chat.dto.ChatMessagePageResponse;
import shop.matjalalzz.chat.dto.ChatMessageRequest;
import shop.matjalalzz.chat.dto.ChatMessageResponse;
import shop.matjalalzz.chat.dto.StompPrincipal;
import shop.matjalalzz.chat.entity.MessageType;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.exception.dto.ErrorResponse;
import shop.matjalalzz.global.security.PrincipalUser;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController implements ChatControllerSpec {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;
    private final MessageChannel clientOutboundChannel;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest message,
        StompPrincipal user) {
        log.trace("Sending message: {}", message);

        ChatMessageResponse messageResponse = chatCommandService.sendMessage(message, user.getId());
        messagingTemplate.convertAndSend("/topic/party/" + message.partyId(), messageResponse);
    }

    @Override
    public BaseResponse<List<ChatMessageResponse>> restoreChat(
        @PathVariable Long partyId,
        @AuthenticationPrincipal PrincipalUser user) {

        List<ChatMessageResponse> chatMessages = chatQueryService.restoreMessages(partyId,
            user.getId());

        return BaseResponse.ok(chatMessages, BaseStatus.OK);
    }

    @Override
    public BaseResponse<ChatMessagePageResponse> loadChatHistory(@PathVariable Long partyId,
        @RequestParam Long cursor,
        @AuthenticationPrincipal PrincipalUser user) {

        ChatMessagePageResponse chatMessages = chatQueryService.loadMessages(partyId, cursor,
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

        ChatMessageResponse message = ChatMessageResponse.builder()
            .sendAt(LocalDateTime.now())
            .type(MessageType.ERROR)
            .message(response.toString())
            .build();

        //유저 전체 세션이 아닌 메세지를 보낸 개별세션에 전송
        Message<?> message1 = messagingTemplate.getMessageConverter()
            .toMessage(message, accessor.getMessageHeaders());

        clientOutboundChannel.send(message1);
    }
}
