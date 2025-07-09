package shop.matjalalzz.domain.party.mapper;

import org.springframework.stereotype.Component;
import shop.matjalalzz.domain.party.dto.PartyCreateRequest;
import shop.matjalalzz.domain.party.dto.PartyDetailResponse;
import shop.matjalalzz.domain.party.dto.PartyListResponse;
import shop.matjalalzz.domain.party.entity.Party;
import shop.matjalalzz.domain.party.entity.PartyUser;
import shop.matjalalzz.domain.party.mock.entity.MockShop;

@Component
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
            .minCount(request.minCount())
            .maxCount(request.maxCount())
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
                .orElse(null) //todo 예외 던져주는 로직 필요
            )
            .build();
    }

    public PartyListResponse toListResponse(Party party) {
        return PartyListResponse.builder()
            .partyId(party.getId())
            .title(party.getTitle())
            .status(party.getStatus())
            .minCount(party.getMinCount())
            .maxCount(party.getMaxCount())
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
            .shopName(party.getShop().getName())
            .shopAddress(party.getShop().getAddress())
            .build();
    }

}
