package shop.matjalalzz.party.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.party.dto.MyPartyPageResponse;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.dto.PartyCreateRequest;
import shop.matjalalzz.party.dto.PartyDetailResponse;
import shop.matjalalzz.party.dto.PartyListResponse;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.shop.entity.Shop;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartyMapper {

    public static Party toEntity(PartyCreateRequest request, Shop shop) {

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

    public static PartyDetailResponse toDetailResponse(Party party, Long hostId,
        String shopImage) {
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
            .hostId(hostId)
            .shopId(party.getId())
            .shopName(party.getShop().getShopName())
            .shopRoadAddress(party.getShop().getRoadAddress())
            .shopDetailAddress(party.getShop().getDetailAddress())
            .shopImage(shopImage)
            .build();
    }

    public static PartyListResponse toListResponse(Party party, String shopImage) {
        return PartyListResponse.builder()
            .partyId(party.getId())
            .title(party.getTitle())
            .status(party.getStatus())
            .minCount(party.getMinCount())
            .maxCount(party.getMaxCount())
            .currentCount(party.getCurrentCount())
            .metAt(party.getMetAt())
            .shopName(party.getShop().getShopName())
            .shopRoadAddress(party.getShop().getRoadAddress())
            .shopDetailAddress(party.getShop().getDetailAddress())
            .shopImage(shopImage)
            .build();
    }

    public static MyPartyPageResponse toMyPartyPageResponse(Long nextCursor,
        Slice<MyPartyResponse> parties) {
        return MyPartyPageResponse.builder()
            .nextCursor(nextCursor)
            .content(parties.getContent())
            .build();
    }
}
