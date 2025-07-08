package shop.matjalalzz.domain.party.mapper;

import shop.matjalalzz.domain.party.dto.PartyCreateRequest;
import shop.matjalalzz.domain.party.dto.PartyDetailResponse;
import shop.matjalalzz.domain.party.entity.Party;
import shop.matjalalzz.domain.party.entity.PartyUser;
import shop.matjalalzz.domain.party.mock.entity.MockShop;

public class PartyMapper {

    public static Party toEntity(PartyCreateRequest request, MockShop shop) {

        return Party.builder()
            .title(request.title())
            .shop(shop)
            .deadline(request.deadline())
            .genderCondition(request.genderCondition())
            .minAge(request.minAge())
            .maxAge(request.maxAge())
            .metAt(request.metAt())
            .description(request.description())
            .build();
    }

    public static PartyDetailResponse toDetailResponse(Party party) {
        return PartyDetailResponse.builder()
            .partyId(party.getId())
            .title(party.getTitle())
            .description(party.getDescription())
            .status(party.getStatus())
            .maxCount(party.getMaxCount())
            .minCount(party.getMinCount())
            .currentCount(party.getCurrentCount())
            .genderCondition(party.getGenderCondition())
            .minAge(party.getMinAge())
            .maxAge(party.getMaxAge())
            .metAt(party.getMetAt())
            .deadline(party.getDeadline())
            .createdAt(party.getCreatedAt())
            .hostId(party.getPartyUsers().stream()
                .filter(PartyUser::isHost)
                .map(pu -> pu.getUser().getId())
                .findFirst()
                .orElse(null)
            )
            .build();
    }

}
