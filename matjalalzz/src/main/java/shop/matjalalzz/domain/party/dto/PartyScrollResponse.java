package shop.matjalalzz.domain.party.dto;

import java.util.List;

public record PartyScrollResponse(
    List<PartyListResponse> content,
    Long nextCursor
) {

}
