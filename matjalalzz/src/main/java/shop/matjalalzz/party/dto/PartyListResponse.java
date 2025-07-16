package shop.matjalalzz.party.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import shop.matjalalzz.party.entity.enums.PartyStatus;

@Builder
public record PartyListResponse(
    Long partyId,
    String title,
    PartyStatus status,
    int maxCount,
    int minCount,
    int currentCount,
    LocalDateTime metAt,
    String shopName,
    String shopRoadAddress,
    String shopDetailAddress
) {

}
