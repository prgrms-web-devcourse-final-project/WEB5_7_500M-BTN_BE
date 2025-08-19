package shop.matjalalzz.party.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.dto.PartyMemberResponse;
import shop.matjalalzz.party.dto.projection.MyPartyProjection;
import shop.matjalalzz.party.dto.projection.PartyMemberProjection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectionToDtoMapper {

    public static MyPartyResponse toMyPartyResponse(MyPartyProjection projection) {
        return MyPartyResponse.builder()
            .partyId(projection.getId())
            .title(projection.getTitle())
            .shopName(projection.getShopName())
            .metAt(projection.getMetAt())
            .deadline(projection.getDeadline())
            .status(projection.getStatus())
            .maxCount(projection.getMaxCount())
            .minCount(projection.getMinCount())
            .currentCount(projection.getCurrentCount())
            .genderCondition(projection.getGenderCondition())
            .minAge(projection.getMinAge())
            .maxAge(projection.getMaxAge())
            .description(projection.getDescription())
            .isHost(projection.getIsHost())
            .build();
    }

    public static PartyMemberResponse toPartyMemberResponse(PartyMemberProjection projection) {
        return PartyMemberResponse.builder()
            .userId(projection.getUserId())
            .userNickname(projection.getUserNickname())
            .userProfile(projection.getUserProfile())
            .isHost(projection.getIsHost())
            .build();
    }

}
