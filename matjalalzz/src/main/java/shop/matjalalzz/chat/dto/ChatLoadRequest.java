package shop.matjalalzz.chat.dto;

import lombok.Builder;

@Builder
public record ChatLoadRequest(
    Long partyId,
    Long lastMessageId
) {

}
