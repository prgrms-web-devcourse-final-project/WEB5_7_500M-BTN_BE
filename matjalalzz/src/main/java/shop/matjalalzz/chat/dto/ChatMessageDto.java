package shop.matjalalzz.chat.dto;

import lombok.Builder;
import shop.matjalalzz.chat.entity.ChatMessage;
import shop.matjalalzz.chat.entity.MessageType;

@Builder
public record ChatMessageDto(
    Long id,
    MessageType type,
    String content,
    String sender,
    Long partyId) {

    public ChatMessage toEntity() {
        return ChatMessage.builder()
            .message(content)
            .sender(sender)
            .partyId(partyId)
            .build();
    }

    public static ChatMessageDto fromEntity(ChatMessage entity) {
        return ChatMessageDto.builder()
            .id(entity.getId())
            .type(MessageType.CHAT)
            .content(entity.getMessage())
            .sender(entity.getSender())
            .partyId(entity.getPartyId())
            .build();
    }
}
