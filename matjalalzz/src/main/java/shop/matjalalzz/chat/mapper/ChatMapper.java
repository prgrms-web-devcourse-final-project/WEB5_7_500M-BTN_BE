package shop.matjalalzz.chat.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.chat.dto.ChatMessagePageResponse;
import shop.matjalalzz.chat.dto.ChatMessageRequest;
import shop.matjalalzz.chat.dto.ChatMessageResponse;
import shop.matjalalzz.chat.entity.ChatMessage;
import shop.matjalalzz.chat.entity.MessageType;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatMapper {

    public static ChatMessage fromChatMessageRequest(ChatMessageRequest request, User sender,
        Party party) {
        return ChatMessage.builder()
            .message(request.message())
            .type(MessageType.CHAT)
            .sender(sender)
            .party(party)
            .build();
    }

    public static ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
            .id(chatMessage.getId())
            .message(chatMessage.getMessage())
            .type(chatMessage.getType())
            .userId(chatMessage.getSender().getId())
            .userNickname(chatMessage.getSender().getNickname())
            .partyId(chatMessage.getParty().getId())
            .userProfile(chatMessage.getSender().getProfileKey())
            .build();
    }

    public static ChatMessagePageResponse toChatMessagePageResponse(
        Slice<ChatMessage> chatMessages, Long nextCursor) {
        return ChatMessagePageResponse.builder()
            .nextCursor(nextCursor)
            .content(chatMessages.stream()
                .map(ChatMapper::toChatMessageResponse).toList()).build();
    }
}
