package shop.matjalalzz.party.app;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.dao.PartySpecification;
import shop.matjalalzz.party.dao.PartyUserRepository;
import shop.matjalalzz.party.dto.MyPartyPageResponse;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.dto.PartyCreateRequest;
import shop.matjalalzz.party.dto.PartyDetailResponse;
import shop.matjalalzz.party.dto.PartyListResponse;
import shop.matjalalzz.party.dto.PartyScrollResponse;
import shop.matjalalzz.party.dto.PartySearchParam;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.mapper.PartyMapper;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository partyRepository;
    private final PartyUserRepository partyUserRepository;
    private final PartySchedulerService partySchedulerService;
    private final ShopService shopService;
    private final UserService userService;
    private final ImageRepository imageRepository;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public void createParty(PartyCreateRequest request, long userId) {

        validateCreateParty(request);

        Shop shop = shopService.shopFind(request.shopId());

        Party party = PartyMapper.toEntity(request, shop);

        PartyUser host = PartyUser.createHost(party, userService.getUserById(userId));
        party.getPartyUsers().add(host);

        partyRepository.save(party);
        partySchedulerService.scheduleDeadlineJob(party);
    }

    @Transactional(readOnly = true)
    public PartyDetailResponse getPartyDetail(Long partyId) {
        Party party = findById(partyId);

        // party host인 유저의 id 찾기
        Long hostId = party.getPartyUsers().stream()
            .filter(PartyUser::isHost)
            .map(pu -> pu.getUser().getId())
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));

        return PartyMapper.toDetailResponse(party, hostId, getShopThumbnail(party));
    }

    @Transactional(readOnly = true)
    public PartyScrollResponse searchParties(PartySearchParam condition, int size) {
        Specification<Party> spec = PartySpecification.createSpecification(condition);

        Pageable pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "id"));
        Slice<Party> partyList = partyRepository.findAll(spec, pageable);

        Long nextCursor = null;
        if (partyList.hasNext()) {
            nextCursor = partyList.getContent().getLast().getId();
        }

        List<PartyListResponse> content = partyList.stream()
            .map(party -> PartyMapper.toListResponse(party, getShopThumbnail(party)))
            .toList();

        return new PartyScrollResponse(content, nextCursor);
    }

    @Transactional(readOnly = true)
    public MyPartyPageResponse findMyPartyPage(Long userId, Long cursor, int size) {
        Slice<MyPartyResponse> parties = partyRepository.findByUserIdAndCursor(userId, cursor,
            PageRequest.of(0, size));

        Long nextCursor = null;
        if (parties.hasNext()) {
            nextCursor = parties.getContent().getLast().partyId();
        }

        return PartyMapper.toMyPartyPageResponse(nextCursor, parties);
    }

    //TODO: 예약금 지불에 대한 로직 필요 (totalReservationFee 올려줘야함)
    @Transactional
    public void joinParty(Long partyId, long userId) {
        Party party = findById(partyId);
        User user = userService.getUserById(userId);

        validateJoinParty(party, user);

        HandlePartyUserJoin(party, user);
    }

    //TODO: 예약금 차감에 대한 로직 필요 (totalReservationFee 내려줘야함)
    @Transactional
    public void quitParty(Long partyId, long userId) {
        Party party = findById(partyId);
        userService.getUserById(userId); //검증용
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우 파티 탈퇴 불가능
        if (partyUser.isHost()) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_QUIT_PARTY);
        }

        // 모집 중인 파티만 탈퇴 가능
        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.CANNOT_QUIT_PARTY_STATUS);
        }

        partyUser.delete();
        party.decreaseCurrentCount();
    }

    // TODO: 예약금 환불 로직 필요
    @Transactional
    public void deleteParty(Long partyId, long userId) {
        Party party = findById(partyId);
        userService.getUserById(userId); //검증용
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우만 파티 삭제 가능
        if (!partyUser.isHost()) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS_DELETE_PARTY);
        }

        // 모집 중인 파티만 삭제 가능
        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_PARTY_STATUS);
        }
        party.deleteParty(); //파티 유저와 댓글까지 cascade 삭제
    }

    @Transactional
    public void completePartyRecruit(Long partyId, long userId) {
        Party party = findById(partyId);

        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.ALREADY_COMPLETE_PARTY);
        }

        userService.getUserById(userId); //검증용
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우만 파티 상태 변경 가능
        if (partyUser.isHost()) {
            party.complete();
        } else {
            throw new BusinessException(ErrorCode.CANNOT_COMPLETE_PARTY);
        }
    }

    //TODO: 추후 imageService로 이동
    private String getShopThumbnail(Party party) {
        return imageRepository.findByShopIdAndImageIndex(party.getShop().getId(), 0)
            .map(image -> BASE_URL + image.getS3Key())
            .orElse(null);
    }

    private void validateJoinParty(Party party, User user) {
        // 1. 모집 상태 확인
        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.NOT_RECRUITING_PARTY);
        }

        // 2. 정원 확인
        if (party.getCurrentCount() >= party.getMaxCount()) {
            throw new BusinessException(ErrorCode.FULL_COUNT_PARTY);
        }

        // 3. 모집 마감 시간 확인
        if (LocalDateTime.now().isAfter(party.getDeadline())) {
            throw new BusinessException(ErrorCode.DEADLINE_GONE);
        }

        // 4. 성별 조건 확인
        GenderCondition condition = party.getGenderCondition();
        if (!condition.equals(GenderCondition.A) &&
            !user.getGender().name().equals(condition.name())) {
            throw new BusinessException(ErrorCode.NOT_MATCH_GENDER);
        }

        // 5. 나이 조건 확인
        int userAge = user.getAge();
        if (userAge < party.getMinAge() || userAge > party.getMaxAge()) {
            throw new BusinessException(ErrorCode.NOT_MATCH_AGE);
        }
    }

    private void validateCreateParty(PartyCreateRequest request) {
        if (request.deadline().isAfter(request.metAt())) {
            throw new BusinessException(ErrorCode.INVALID_DEADLINE);
        }
        if (request.minAge() > request.maxAge()) {
            throw new BusinessException(ErrorCode.INVALID_AGE_CONDITION);
        }
        if (request.minCount() > request.maxCount()) {
            throw new BusinessException(ErrorCode.INVALID_COUNT_CONDITION);
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

        party.increaseCurrentCount();
    }

    private PartyUser findPartyUser(long userId, Party party) {
        return party.getPartyUsers().stream()
            .filter(pu -> pu.getUser().getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOIN_PARTY));
    }

    @Transactional(readOnly = true)
    public Party findById(Long partyId) {
        return partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PartyUser> getPartyUsers(Long partyId) {
        return partyUserRepository.findAllByPartyId(partyId);
    }

    @Transactional(readOnly = true)
    public boolean isInParty(Long partyId, Long userId) {
        return partyUserRepository.existsByUserIdAndPartyId(userId, partyId);
    }
}
