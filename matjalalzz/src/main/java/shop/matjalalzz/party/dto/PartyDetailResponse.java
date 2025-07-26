package shop.matjalalzz.party.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;

@Builder
public record PartyDetailResponse(
    Long partyId,
    String title,
    String description,
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
    Long shopId,
    String shopName,
    String shopRoadAddress,
    String shopDetailAddress,
    String shopImage,
    List<PartyMemberResponse> members
) {

}
