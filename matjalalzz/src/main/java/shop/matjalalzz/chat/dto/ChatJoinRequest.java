package shop.matjalalzz.chat.dto;

import lombok.Builder;

@Builder
public record ChatJoinRequest(
    Long partyId
) {

}
