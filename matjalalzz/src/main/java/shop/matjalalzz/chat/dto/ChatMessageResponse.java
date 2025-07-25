package shop.matjalalzz.chat.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import shop.matjalalzz.chat.entity.MessageType;

@Builder
public record ChatMessageResponse(
    Long id,
    LocalDateTime sendAt,
    MessageType type,
    String message,
    Long userId,
    String userNickname,
    String userProfile,
    Long partyId) {

}
