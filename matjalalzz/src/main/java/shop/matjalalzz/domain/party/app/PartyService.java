package shop.matjalalzz.domain.party.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.domain.party.dao.PartyRepository;
import shop.matjalalzz.domain.party.dto.PartyCreateRequest;
import shop.matjalalzz.domain.party.dto.PartyDetailResponse;
import shop.matjalalzz.domain.party.entity.Party;
import shop.matjalalzz.domain.party.entity.PartyUser;
import shop.matjalalzz.domain.party.mapper.PartyMapper;
import shop.matjalalzz.domain.shop.Shop;
import shop.matjalalzz.domain.shop.ShopRepository;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.PartyErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {

    private final ShopRepository shopRepository;
    private final PartyRepository partyRepository;

    @Transactional
    public void createParty(PartyCreateRequest request) {

        if (request.deadline().isAfter(request.metAt())) {
            throw new BusinessException(PartyErrorCode.INVALID_DEADLINE);
        }

        Shop shop = shopRepository.findById(request.shopId()).orElseThrow(() ->
            new BusinessException(PartyErrorCode.SHOP_NOT_FOUND));

        Party party = PartyMapper.toEntity(request, shop);

        PartyUser host = PartyUser.createHost(party);
        party.getPartyUsers().add(host);

        partyRepository.save(party);
    }

    public PartyDetailResponse getPartyDetail(Long partyId) {
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(PartyErrorCode.PARTY_NOT_FOUND));

        return null;
    }
}
