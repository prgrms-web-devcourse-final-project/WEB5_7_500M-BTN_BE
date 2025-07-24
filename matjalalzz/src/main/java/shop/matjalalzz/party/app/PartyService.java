package shop.matjalalzz.party.app;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.dao.PartySpecification;
import shop.matjalalzz.party.dao.PartyUserRepository;
import shop.matjalalzz.party.dto.KickoutResponse;
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
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.party.mapper.PartyMapper;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
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
    private final ReservationRepository reservationRepository;

    private final String MAX_ATTEMPTS = "${custom.retry.max-attempts}";
    private final String MAX_DELAY = "${custom.retry.max-delay}";
    private final String MULTIPLIER = "${custom.retry.multiplier}";

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

        // todo: 채팅방 생성
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

    @Retryable(
        retryFor = DataAccessException.class,
        maxAttemptsExpression = MAX_ATTEMPTS,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public void joinParty(Long partyId, long userId) {
        Party party = findById(partyId);
        User user = userService.getUserById(userId);

        validateJoinParty(party, user);

        PartyUser partyUser = PartyUser.createUser(party, user);
        party.getPartyUsers().add(partyUser);
        party.increaseCurrentCount();

        // todo: 채팅방 참여 로직 추가
    }

    @Retryable(
        retryFor = DataAccessException.class,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public void quitParty(Long partyId, long userId) {
        Party party = findByIdWithReservationAndPartyUsers(partyId);
        userService.getUserById(userId); //검증용
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우 파티 탈퇴 불가능
        if (partyUser.isHost()) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_QUIT_PARTY);
        }

        Reservation reservation = party.getReservation();

        // 예약이 없거나, 아직 예약이 승인 대기 중일 때만 탈퇴 가능
        if (reservation != null && reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.CANNOT_QUIT_PARTY_STATUS);
        }

        // 파티 나갔을 때, 최소 인원보다 적을 시 파티 제거
        if(party.getMinCount() > party.getCurrentCount() - 1) {
            breakParty(party);
            return;
        }

        if (partyUser.isPaymentCompleted()) {
            int fee = party.getTotalReservationFee() / party.getCurrentCount();
            party.decreaseTotalReservationFee(fee);

            if (reservation != null) {
                reservation.decreaseHeadCount();
                reservation.decreaseReservationFee(fee);
            }
        }

        partyUser.delete();
        party.decreaseCurrentCount();

        // todo: 파티 탈퇴 시, 채팅 메시지는 어떻게 할 지 상의 필요
    }

    @Retryable(
        retryFor = DataAccessException.class,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public KickoutResponse kickout(Long partyId, long userId, long kickoutUserId) {
        // 본인 강퇴 불가
        if (userId == kickoutUserId) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_OUT_SELF);
        }

        Party party = findById(partyId);
        PartyUser partyUser = findPartyUser(userId, party);

        // 파티장만 강퇴 가능
        if (!partyUser.isHost()) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS_KICK_OUT_PARTY_USER);
        }

        PartyUser kickoutPartyUser = findPartyUser(kickoutUserId, party);

        // 예약금 결제 완료한 팀원은 강퇴 불가
        if (kickoutPartyUser.isPaymentCompleted()) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_OUT_PAYMENT_COMPLETE);
        }

        // 강퇴 후, 최소 인원 불만족 시 파티 해체
        if (party.getStatus() == PartyStatus.COMPLETED
            && party.getMinCount() > party.getCurrentCount() - 1) {
            breakParty(party);
            return new KickoutResponse(true);
        }

        partyUser.delete();
        // todo: 파티 유저 탈퇴시 채팅 메시지 처리 방식 상의

        return new KickoutResponse(false);
    }

    @Retryable(
        retryFor = DataAccessException.class,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public void breakParty(Party party) {
        // 삭제됐거나 종료된 상태의 파티는 삭제 불가
        if(party.isDeleted() || party.getStatus() == PartyStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_PARTY_TERMINATED);
        }

        Reservation reservation = party.getReservation();

        // 예약일 하루 전이라면 파티 삭제 불가
        if(reservation != null && reservation.getStatus() == ReservationStatus.CONFIRMED
            && reservation.getReservedAt().isBefore(LocalDateTime.now().plusDays(1))) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_PARTY_D_DAY);
        }

        // todo: 이런 부분도 퍼사드 패턴 적용해서 책임 분리 필요
        // 예약금 환불
        int fee = party.getShop().getReservationFee();
        partyUserRepository.refundReservationFee(party.getId(), fee);

        // 예약 취소
        if(reservation != null) {
            reservation.changeStatus(ReservationStatus.CANCELLED);
        }

        party.deleteParty();
    }

    @Retryable(
        retryFor = DataAccessException.class,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public void deleteParty(Long partyId, long userId) {
        Party party = findByIdWithPartyUsers(partyId);
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우만 파티 삭제 가능
        if (!partyUser.isHost()) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS_DELETE_PARTY);
        }

        breakParty(party);
    }

    @Transactional
    public void completePartyRecruit(Long partyId, long userId) {
        Party party = findByIdWithPartyUsers(partyId);

        // 파티 상태가 모집 중인지 검사
        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.ALREADY_COMPLETE_PARTY);
        }

        userService.getUserById(userId); //검증용
        PartyUser partyUser = findPartyUser(userId, party);

        // 호스트인 경우만 파티 상태 변경 가능
        if (!partyUser.isHost()) {
            throw new BusinessException(ErrorCode.CANNOT_COMPLETE_PARTY);
        }

        // 파티 최소 인원 충족 검사
        if (party.getCurrentCount() < party.getMinCount()) {
            throw new BusinessException(ErrorCode.CANNOT_CHANGE_PARTY_STATUS_MIN_COUNT);
        }

        party.complete();
    }

    @Retryable(
        retryFor = DataAccessException.class,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public void payReservationFee(Long partyId, long userId) {
        User user = userService.getUserById(userId);
        Party party = findByIdWithPartyUsers(partyId);

        // 파티 모집 중일 때는 예약금 지불 불가
        if(party.getStatus() == PartyStatus.RECRUITING) {
            throw new BusinessException(ErrorCode.CANNOT_PAY_FEE_RECRUITING);
        }

        PartyUser partyUser = findPartyUser(userId, party);

        // 이미 예약금을 지불한 파티원의 중복 지불 방지
        if (partyUser.isPaymentCompleted()) {
            throw new BusinessException(ErrorCode.ALREADY_PAID_USER);
        }

        int reservationFee = party.getShop().getReservationFee();

        // 파티원의 보유 포인트 확인
        if (user.getPoint() < reservationFee) {
            throw new BusinessException(ErrorCode.LACK_OF_BALANCE);
        }

        user.decreasePoint(reservationFee);
        party.increaseTotalReservationFee(reservationFee);
        partyUser.completePayment();

        // 모두 결제 했으면, 예약 자동 생성
        /*
        todo: ReservationService 에서 PartyService 를 의존성 주입받고 있어서,
              순환 참조 문제로 ReservationService을 직접 의존 불가
              repository를 직접 사용하는 예약 생성 로직을 퍼사드 패턴으로 리팩토링 필요
        */
        boolean allPaid = party.getPartyUsers().stream().allMatch(PartyUser::isPaymentCompleted);
        if (allPaid) {
            User host = findPartyHost(partyId);
            Reservation reservation = ReservationMapper.toEntity(party, host);
            reservationRepository.save(reservation);
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

        // 6. 중복 참여, 이전에 참여 이력 존재 확인
        Optional<PartyUser> existingPartyUser = partyUserRepository.findByUserIdAndPartyId(
            user.getId(), party.getId());
        if(existingPartyUser.isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_PARTY_USER);
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

    private Party findByIdWithReservationAndPartyUsers(Long partyId) {
        return partyRepository.findPartyByIdWithReservationAndPartyUsers(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    private Party findByIdWithPartyUsers(Long partyId) {
        return partyRepository.findPartyByIdWithPartyUsers(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    private PartyUser findPartyUser(long userId, Party party) {
        return party.getPartyUsers().stream()
            .filter(pu -> pu.getUser().getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOIN_PARTY));
    }

    private User findPartyHost(long partyId) {
        return partyUserRepository.findPartyHostByPartyId(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
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
