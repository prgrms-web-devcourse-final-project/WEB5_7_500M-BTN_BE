package shop.matjalalzz.domain.party.dto;

import java.time.LocalDateTime;
import shop.matjalalzz.domain.party.entity.GenderCondition;
import shop.matjalalzz.domain.party.entity.PartyStatus;

public record PartyScrollResponse(
    Long partyId,
    String title,
    PartyStatus status,
    int maxCount,
    int minCount,
    int currentCount,
    GenderCondition genderCondition,
    int minAge,
    int maxAge,
    LocalDateTime deadline,
    LocalDateTime createdAt
//    Long hostId,
//    String shopName,
//    String shopAdderess
) {

}
