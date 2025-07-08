package shop.matjalalzz.domain.party.app;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.domain.party.dao.PartyRepository;
import shop.matjalalzz.domain.party.dto.PartyCreateRequest;
import shop.matjalalzz.domain.party.dto.PartyDetailResponse;
import shop.matjalalzz.domain.party.entity.GenderCondition;
import shop.matjalalzz.domain.party.entity.Party;
import shop.matjalalzz.domain.party.entity.PartyStatus;
import shop.matjalalzz.domain.party.entity.PartyUser;
import shop.matjalalzz.domain.party.mapper.PartyMapper;
import shop.matjalalzz.domain.shop.Shop;
import shop.matjalalzz.domain.shop.ShopRepository;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {

    private final ShopRepository shopRepository;
    private final PartyRepository partyRepository;

    @Transactional
    public void createParty(PartyCreateRequest request) {

        if (request.deadline().isAfter(request.metAt())) {
            throw new BusinessException(ErrorCode.INVALID_DEADLINE);
        }

        Shop shop = shopRepository.findById(request.shopId()).orElseThrow(() ->
            new BusinessException(ErrorCode.DATA_NOT_FOUND)); //todo 추후 shopService로 이동

        Party party = PartyMapper.toEntity(request, shop);

        PartyUser host = PartyUser.createHost(party); //todo userId 필요
        party.getPartyUsers().add(host);

        partyRepository.save(party);
    }

    public PartyDetailResponse getPartyDetail(Long partyId) {
        return PartyMapper.toDetailResponse(findById(partyId));
    }

    public void searchParties(PartyStatus status, GenderCondition gender, String location,
        String category, String query, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1, Sort.by(Direction.DESC, "id"));
    }

    @Transactional
    public void joinParty(Long partyId) {
        Party party = findById(partyId);
        PartyUser user = PartyUser.createUser(party);
        party.getPartyUsers().add(user);
    }

    private Party findById(Long partyId) {
        return partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }
}
