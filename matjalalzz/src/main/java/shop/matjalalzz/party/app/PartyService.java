package shop.matjalalzz.party.app;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.dao.PartyUserRepository;
import shop.matjalalzz.party.dto.PartyCreateRequest;
import shop.matjalalzz.party.dto.PartyDetailResponse;
import shop.matjalalzz.party.dto.PartyListResponse;
import shop.matjalalzz.party.dto.PartyScrollResponse;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.party.mapper.PartyMapper;
import shop.matjalalzz.party.mock.dao.MockShopRepository;
import shop.matjalalzz.party.mock.entity.MockShop;
import shop.matjalalzz.party.util.ScrollPaginationCollection;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {

    private final MockShopRepository shopRepository;
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final PartyUserRepository partyUserRepository;

    private final PartyMapper partyMapper;

    @Transactional
    public void createParty(PartyCreateRequest request, long userId) {

        if (request.deadline().isAfter(request.metAt())) {
            throw new BusinessException(ErrorCode.INVALID_DEADLINE);
        }

        // request.shopId()를 1L로 대체
        MockShop shop = shopRepository.findById(1L).orElseThrow(() ->
            new BusinessException(ErrorCode.DATA_NOT_FOUND)); //todo 추후 shopService로 이동

        Party party = PartyMapper.toEntity(request, shop);

//        MockUser user = findOwnerById();
        PartyUser host = PartyUser.createHost(party, getUserById(userId));
        party.getPartyUsers().add(host);

        partyRepository.save(party);
    }

    public PartyDetailResponse getPartyDetail(Long partyId) {
        return PartyMapper.toDetailResponse(findById(partyId));
    }

    public PartyScrollResponse searchParties(PartyStatus status, GenderCondition gender,
        String location,
        String category, String query, Long cursor, int size) {

//        Pageable pageable = PageRequest.of(0, size + 1, Sort.by(Direction.DESC, "id"));
        List<Party> result = partyRepository.findAll();//todo 필터링한 결과값으로 변경

        ScrollPaginationCollection<Party> scroll = ScrollPaginationCollection.of(
            result, size);

        List<PartyListResponse> content = scroll.getCurrentScrollItems().stream()
            .map(partyMapper::toListResponse)
            .toList();

        Long nextCursor = scroll.isLastScroll() ? null : scroll.getNextCursor().getId();

        return new PartyScrollResponse(content, nextCursor);
    }

    @Transactional
    public void joinParty(Long partyId, long userId) {
        Party party = findById(partyId);
        User user = getUserById(userId);

        HandlePartyUserJoin(party, user);
    }

    //이미 파티에 참여중인 유저인지 검증
    private void HandlePartyUserJoin(Party party, User user) {
        Optional<PartyUser> existingPartyUser = partyUserRepository.findByUserIdAndPartyId(
            user.getId(), party.getId());

        if (existingPartyUser.isPresent()) {
            PartyUser partyUser = existingPartyUser.get();
            if (!partyUser.isDeleted()) {
                throw new BusinessException(ErrorCode.ALREADY_PARTY_USER);
            }
            partyUser.setDeleted(false); //파티 탈퇴한 사람이 다시 파티 참여한 경우
        } else {
            PartyUser partyUser = PartyUser.createUser(party, user);
            party.getPartyUsers().add(partyUser);
        }
    }

    @Transactional
    public void quitParty(Long partyId, long userId) {
        Party party = findById(partyId);

        // todo 호스트는 파티 탈퇴를 못하게 할지?
        party.getPartyUsers().stream()
            .filter(pu -> pu.getUser().getId().equals(userId))
            .findFirst()
            .ifPresent(pu -> pu.setDeleted(true));
    }

    @Transactional
    public void deleteParty(Long partyId) {
        //todo 파티 삭제는 host만 가능
        Party party = findById(partyId);
        party.setDeleted(true);
    }

    @Transactional
    public void completePartyRecruit(Long partyId) {
        //todo 파티 상태 변경은 host만 가능
        Party party = findById(partyId);
        party.setStatus(PartyStatus.COMPLETED);
    }

    private Party findById(Long partyId) {
        return partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
            new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }
}
