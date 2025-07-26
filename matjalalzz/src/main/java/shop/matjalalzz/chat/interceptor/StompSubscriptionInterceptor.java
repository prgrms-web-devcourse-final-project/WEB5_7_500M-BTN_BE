package shop.matjalalzz.chat.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import shop.matjalalzz.chat.dto.StompPrincipal;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.dao.PartyUserRepository;

@Component
@Slf4j
@RequiredArgsConstructor
public class StompSubscriptionInterceptor implements ChannelInterceptor {

    // PartyService를 주입받으면, 순환참조 문제가 발생해서 임시로 변경
    private final PartyUserRepository partyUserRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
            StompHeaderAccessor.class);

        if (accessor.getCommand().equals(StompCommand.SUBSCRIBE)) {
            validateUser(accessor);
            validatePartyUser(accessor);
        }

        return message;
    }

    private void validateUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            log.debug("User not logged in");
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private void validatePartyUser(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination != null && destination.contains("/topic/party/")) {
            Long partyId = Long.parseLong(
                destination.substring(destination.indexOf("/party/") + 7));
            Long userId = ((StompPrincipal) accessor.getUser()).getId();
            if (!partyUserRepository.existsByUserIdAndPartyId(userId, partyId)) {
                log.debug("User not in Party");
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            }
        }
    }
}
