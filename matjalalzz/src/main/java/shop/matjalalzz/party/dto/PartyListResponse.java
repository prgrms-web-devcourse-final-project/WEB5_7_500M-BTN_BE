package shop.matjalalzz.party.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;

@Builder
public record PartyListResponse(
    Long partyId,
    String title,
    PartyStatus status,
    int maxCount,
    int minCount,
    int currentCount,
    GenderCondition genderCondition,
    int minAge,
    int maxAge,
    LocalDateTime metAt,
    LocalDateTime deadline,
    LocalDateTime createdAt,
    Long hostId,
    String shopName,
    String shopAddress
) {

}
