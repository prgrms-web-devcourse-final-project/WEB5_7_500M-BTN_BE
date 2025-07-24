package shop.matjalalzz.chat.dto;

import lombok.Builder;

@Builder
public record ChatMessageRequest(
    String message,
    Long partyId) {

}
