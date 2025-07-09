package shop.matjalalzz.party.dto;

import java.util.List;

public record PartyScrollResponse(
    List<PartyListResponse> content,
    Long nextCursor
) {

}
