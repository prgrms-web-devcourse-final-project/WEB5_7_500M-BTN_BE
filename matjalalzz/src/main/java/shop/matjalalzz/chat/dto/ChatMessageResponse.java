package shop.matjalalzz.chat.dto;

import lombok.Builder;
import shop.matjalalzz.chat.entity.MessageType;

@Builder
public record ChatMessageResponse(
    Long id,
    MessageType type,
    String message,
    Long userId,
    String userNickname,
    String userProfile,
    Long partyId) {

}
