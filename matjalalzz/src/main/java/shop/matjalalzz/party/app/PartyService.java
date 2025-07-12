package shop.matjalalzz.party.app;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.dao.PartySpecification;
import shop.matjalalzz.party.dao.PartyUserRepository;
import shop.matjalalzz.party.dto.PartyCreateRequest;
import shop.matjalalzz.party.dto.PartyDetailResponse;
import shop.matjalalzz.party.dto.PartyListResponse;
import shop.matjalalzz.party.dto.PartyScrollResponse;
import shop.matjalalzz.party.dto.PartySearchCondition;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.party.mapper.PartyMapper;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class PartyService {

    private final ShopRepository shopRepository;
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final PartyUserRepository partyUserRepository;

    @Transactional
    public void createParty(PartyCreateRequest request, long userId) {

        if (request.deadline().isAfter(request.metAt())) {
            throw new BusinessException(ErrorCode.INVALID_DEADLINE);
        }

        //todo: 추후 shopService로 이동 및 mockShop 제거
        Shop shop = shopRepository.findById(request.shopId()).orElseThrow(() ->
            new BusinessException(ErrorCode.DATA_NOT_FOUND));

        Party party = PartyMapper.toEntity(request, shop);

        PartyUser host = PartyUser.createHost(party, getUserById(userId));
        party.getPartyUsers().add(host);

        partyRepository.save(party);
    }

    @Transactional(readOnly = true)
    public PartyDetailResponse getPartyDetail(Long partyId) {
        return PartyMapper.toDetailResponse(findById(partyId));
    }

    @Transactional(readOnly = true)
    public PartyScrollResponse searchParties(PartySearchCondition condition, int size) {
        Specification<Party> spec = PartySpecification.createSpecification(condition);

        Pageable pageable = PageRequest.of(0, size + 1, Sort.by(Direction.DESC, "id"));
        List<Party> partyList = partyRepository.findAll(spec, pageable).getContent();

        boolean hasNext = partyList.size() > size;
        Long nextCursor = null;

        if (hasNext) {
            nextCursor = partyList.get(size - 1).getId();
            partyList = partyList.subList(0, size);
        }

        List<PartyListResponse> content = partyList.stream()
            .map(PartyMapper::toListResponse)
            .toList();

        return new PartyScrollResponse(content, nextCursor);
    }

    @Transactional
    public void joinParty(Long partyId, long userId) {
        Party party = findById(partyId);
        User user = getUserById(userId);

        HandlePartyUserJoin(party, user);
    }

    @Transactional
    public void quitParty(Long partyId, long userId) {
        Party party = findById(partyId);
        getUserById(userId); //검증용
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우 파티 탈퇴 불가능
        if (partyUser.isHost()) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_QUIT_PARTY);
        }
        partyUser.delete();
    }

    @Transactional
    public void deleteParty(Long partyId, long userId) {
        Party party = findById(partyId);
        getUserById(userId); //검증용
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우만 파티 삭제 가능
        if (partyUser.isHost()) {
            party.deleteParty(); //파티 유저까지 cascade 삭제
        } else {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_PARTY);
        }
    }

    @Transactional
    public void completePartyRecruit(Long partyId, long userId) {
        Party party = findById(partyId);
        getUserById(userId); //검증용
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우만 파티 상태 변경 가능
        if (partyUser.isHost()) {
            party.complete();
        } else {
            throw new BusinessException(ErrorCode.CANNOT_COMPLETE_PARTY);
        }
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
            partyUser.recover(); //파티 탈퇴한 사람이 다시 파티 참여한 경우
        } else {
            PartyUser partyUser = PartyUser.createUser(party, user);
            party.getPartyUsers().add(partyUser);
        }
    }

    private PartyUser findPartyUser(long userId, Party party) {
        return party.getPartyUsers().stream()
            .filter(pu -> pu.getUser().getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOIN_PARTY));
    }

    private Party findById(Long partyId) {
        return partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
            new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
