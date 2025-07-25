package shop.matjalalzz.chat.dto;

import lombok.Builder;

@Builder
public record ChatRestoreRequest(
    Long partyId,
    Long lastMessageId
) {

}
