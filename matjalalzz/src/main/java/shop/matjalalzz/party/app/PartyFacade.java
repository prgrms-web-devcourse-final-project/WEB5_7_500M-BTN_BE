package shop.matjalalzz.party.app;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.chat.app.PartyChatService;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.image.app.ImageFacade;

import shop.matjalalzz.party.dto.MyPartyPageResponse;
import shop.matjalalzz.party.dto.MyPartyResponse;
import shop.matjalalzz.party.dto.PartyCreateRequest;
import shop.matjalalzz.party.dto.PartyDetailResponse;
import shop.matjalalzz.party.dto.PartyListResponse;
import shop.matjalalzz.party.dto.PartyMemberResponse;
import shop.matjalalzz.party.dto.PartyScrollResponse;
import shop.matjalalzz.party.dto.PartySearchParam;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.party.mapper.PartyMapper;
import shop.matjalalzz.reservation.app.ReservationCommandService;
import shop.matjalalzz.reservation.app.ReservationQueryService;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.app.query.ShopQueryService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyFacade {

    private final PartySchedulerService partySchedulerService;
    private final PartyService partyService;
    private final ShopQueryService shopQueryService;
    private final UserService userService;
    private final PartyChatService partyChatService;
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final ImageFacade imageFacade;

    private final String MAX_ATTEMPTS = "${custom.retry.max-attempts}";
    private final String MAX_DELAY = "${custom.retry.max-delay}";
    private final String MULTIPLIER = "${custom.retry.multiplier}";

    @Transactional
    public void createParty(PartyCreateRequest request, long userId) {

        if (request.deadline().isAfter(request.metAt())) {
            throw new BusinessException(ErrorCode.INVALID_DEADLINE);
        }
        if (request.minAge() > request.maxAge()) {
            throw new BusinessException(ErrorCode.INVALID_AGE_CONDITION);
        }
        if (request.minCount() > request.maxCount()) {
            throw new BusinessException(ErrorCode.INVALID_COUNT_CONDITION);
        }

        User user = userService.getUserById(userId);
        Shop shop = shopQueryService.findShop(request.shopId());
        Party party = PartyMapper.toEntity(request, shop);

        PartyUser host = PartyUser.createHost(party, user);
        party.getPartyUsers().add(host);

        partyService.saveParty(party);
        partySchedulerService.scheduleDeadlineJob(party);

        partyChatService.join(user, party);
    }

    @Transactional(readOnly = true)
    public PartyDetailResponse getPartyDetail(Long partyId) {
        Party party = partyService.findByIdWithShop(partyId);
        List<PartyMemberResponse> members = partyService.findMembersByPartyId(partyId);
        String thumbnailUrl = imageFacade.findByShopThumbnail(party.getShop().getId());

        return PartyMapper.toDetailResponse(party, thumbnailUrl, members);
    }

    @Transactional(readOnly = true)
    public List<PartyMemberResponse> getPartyMembers(Long partyId) {
        return partyService.findMembersByPartyId(partyId);
    }

    @Transactional(readOnly = true)
    public PartyScrollResponse searchParties(PartySearchParam condition, int size) {
        Slice<Party> parties = partyService.searchParties(condition, PageRequest.of(0, size));

        Long nextCursor = null;
        if (parties.hasNext()) {
            nextCursor = parties.getContent().getLast().getId();
        }

        // shopId 수집
        List<Long> shopIds = parties.stream()
            .map(p -> p.getShop().getId())
            .distinct()
            .toList();

        // 썸네일 이미지 한꺼번에 조회
        Map<Long, String> thumbMap = imageFacade.findByShopThumbnails(shopIds);

        List<PartyListResponse> content = parties.stream()
            .map(party -> PartyMapper.toListResponse
                (party, thumbMap.get(party.getShop().getId()))
            )
            .toList();

        return new PartyScrollResponse(content, nextCursor);
    }

    @Transactional(readOnly = true)
    public MyPartyPageResponse findMyPartyPage(Long userId, Long cursor, int size) {
        Slice<MyPartyResponse> parties = partyService.findByUserIdAndCursor(userId, cursor,
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
        Party party = partyService.findById(partyId);
        User user = userService.getUserById(userId);

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
        Optional<Boolean> deletedOrNot = partyService.findByUserIdAndPartyId(user.getId(),
            party.getId());

        //이전에 참여 이력 존재
        if (deletedOrNot.isPresent()) {
            if (deletedOrNot.get()) { // 한번 파티에 참여했다가 탈퇴했던 유저
                throw new BusinessException(ErrorCode.QUIT_PARTY_USER);
            } else { //이미 파티에 참여중인 유저
                throw new BusinessException(ErrorCode.ALREADY_PARTY_USER);
            }
        }

        PartyUser partyUser = PartyUser.createUser(party, user);
        party.getPartyUsers().add(partyUser);
        party.increaseCurrentCount();

        partyChatService.join(user, party);
    }

    @Retryable(
        retryFor = DataAccessException.class,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public void quitParty(Long partyId, long userId) {
        Party party = partyService.findByIdWithPartyUsers(partyId);
        User user = userService.getUserById(userId);
        PartyUser partyUser = partyService.findPartyUser(userId, party);

        // 호스트인 경우 파티 탈퇴 불가능
        if (partyUser.isHost()) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_QUIT_PARTY);
        }

        // 파티가 이미 종료된 경우, 탈퇴 불가능
        if (party.getStatus() == PartyStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        Reservation reservation = reservationQueryService.findByPartyId(partyId);

        // 예약이 없거나, 아직 예약이 승인 대기 중일 때만 탈퇴 가능
        if (reservation != null && reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.CANNOT_QUIT_PARTY_STATUS);
        }

        processQuitParty(partyUser, party, user, reservation);
    }

    @Transactional
    public void quitPartyForWithdraw(User user) {
        // 종료되지 않은 파티 중 본인이 호스트가 아닌 partyUser 조회
        List<PartyUser> participatingPartyUsers = partyService.findAllParticipatingParty(
            user.getId());

        // 탈퇴할 파티가 없을 경우 바로 리턴
        if (participatingPartyUsers.isEmpty()) {
            return;
        }

        // partyId 수집
        List<Long> partyIds = participatingPartyUsers.stream()
            .map(pu -> pu.getParty().getId())
            .distinct()
            .toList();

        // 파티별 예약 map 생성
        Map<Long, Reservation> reservationByPartyId = reservationQueryService.getMapByPartyIds(
            partyIds);

        for (PartyUser pu : participatingPartyUsers) {
            Long partyId = pu.getParty().getId();
            Reservation reservation = reservationByPartyId.get(partyId);

            //PENDING 상태가 아닌 예약을 가진 파티는 탈퇴 불가능
            if (reservation != null && reservation.getStatus() != ReservationStatus.PENDING) {
                continue;
            }

            // 파티 탈퇴 진행
            processQuitParty(pu, pu.getParty(), user, reservation);
        }
    }

    private void processQuitParty(PartyUser partyUser, Party party, User user,
        Reservation reservation) {

        // 파티 모집완료 상태에서 나갔을 때, 최소 인원보다 적을 시 파티 제거
        if (party.getStatus() == PartyStatus.COMPLETED
            && party.getMinCount() > party.getCurrentCount() - 1) {

            reservationCommandService.refundPartyReservationFee(party);

            if (reservation != null) {
                reservation.changeStatus(ReservationStatus.CANCELLED);
            }

            partyService.breakParty(party);
            return;
        }

        if (partyUser.isPaymentCompleted()) {
            int fee = party.getShop().getReservationFee();
            party.decreaseTotalReservationFee(fee);
            user.increasePoint(fee);

            if (reservation != null) {
                reservation.decreaseHeadCount();
                reservation.decreaseReservationFee(fee);
            }
        }

        partyUser.delete();
        party.decreaseCurrentCount();

        partyChatService.leaveParty(user, party);
    }

    @Retryable(
        retryFor = DataAccessException.class,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public void kickout(Long partyId, long hostUserId, long kickoutUserId) {
        // 본인 강퇴 불가
        if (hostUserId == kickoutUserId) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_OUT_SELF);
        }

        Party party = partyService.findById(partyId);
        PartyUser host = partyService.findPartyUser(hostUserId, party);

        // 파티장만 강퇴 가능
        if (!host.isHost()) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS_KICK_OUT_PARTY_USER);
        }

        PartyUser kickoutUser = partyService.findPartyUser(kickoutUserId, party);

        // 예약금 결제 완료한 팀원은 강퇴 불가
        if (kickoutUser.isPaymentCompleted()) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_OUT_PAYMENT_COMPLETE);
        }

        // 강퇴 후, 최소 인원 불만족 시 파티 해체
        if (party.getStatus() == PartyStatus.COMPLETED
            && party.getMinCount() > party.getCurrentCount() - 1) {

            reservationCommandService.refundPartyReservationFee(party);

            partyService.breakParty(party);
            return;
        }

        kickoutUser.delete();
        partyChatService.kickUser(kickoutUser.getUser(), party);
    }

    @Retryable(
        retryFor = DataAccessException.class,
        backoff = @Backoff(
            delayExpression = MAX_DELAY, multiplierExpression = MULTIPLIER, random = true
        )
    )
    @Transactional
    public void deleteParty(Long partyId, long userId) {
        Party party = partyService.findByIdWithPartyUsers(partyId);
        PartyUser partyUser = partyService.findPartyUser(userId, party);

        if (party.getStatus() == PartyStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 호스트인 경우만 파티 삭제 가능
        if (!partyUser.isHost()) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS_DELETE_PARTY);
        }

        Reservation reservation = reservationQueryService.findByPartyId(partyId);

        // 예약일 하루 전이라면 파티 삭제 불가
        if (cannotDeleteParty(reservation)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_PARTY_D_DAY);
        }

        reservationCommandService.refundPartyReservationFee(party);

        if (reservation != null) {
            reservation.changeStatus(ReservationStatus.CANCELLED);
        }

        partyService.breakParty(party);
    }

    @Transactional
    public void deletePartyForWithdraw(User user) {
        // 회원 탈퇴시에 본인이 파티장이며 종료되지 않은 파티만 조회
        List<Party> parties = partyService.findAllMyRecruitingParty(user.getId());

        //partyId 수집
        List<Long> partyIds = parties.stream()
            .map(Party::getId)
            .distinct()
            .toList();

        // 파티별 예약 map 생성
        Map<Long, Reservation> reservationByPartyId = reservationQueryService.getMapByPartyIds(
            partyIds);

        for (Party party : parties) {
            Reservation reservation = reservationByPartyId.get(party.getId());

            //파티의 예약일이 하루 전인 파티는 해체 시키지 않음
            if (cannotDeleteParty(reservation)) {
                continue;
            }

            // 예약금을 지불한 파티원에게 예약금 환불
            reservationCommandService.refundPartyReservationFee(party);

            // 예약이 진행 중일 때, 예약을 취소
            if (reservation != null) {
                reservation.changeStatus(ReservationStatus.CANCELLED);
            }

            // 파티 해체
            partyService.breakParty(party);
        }
    }

    private boolean cannotDeleteParty(Reservation reservation) {
        return reservation != null
            && reservation.getStatus() == ReservationStatus.CONFIRMED
            && reservation.getReservedAt().isBefore(LocalDateTime.now().plusDays(1));
    }

    @Transactional
    public void completePartyRecruit(Long partyId, long userId) {
        Party party = partyService.findByIdWithPartyUsers(partyId);

        // 파티 상태가 모집 중인지 검사
        if (!party.isRecruiting()) {
            throw new BusinessException(ErrorCode.ALREADY_COMPLETE_PARTY);
        }

        User host = userService.getUserById(userId);
        PartyUser partyUser = partyService.findPartyUser(userId, party);

        // 호스트인 경우만 파티 상태 변경 가능
        if (!partyUser.isHost()) {
            throw new BusinessException(ErrorCode.CANNOT_COMPLETE_PARTY);
        }

        // 파티 최소 인원 충족 검사
        if (party.getCurrentCount() < party.getMinCount()) {
            throw new BusinessException(ErrorCode.CANNOT_CHANGE_PARTY_STATUS_MIN_COUNT);
        }

        party.complete();

        partyChatService.noticePaymentRequest(host, party);
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
        Party party = partyService.findByIdWithPartyUsers(partyId);

        // 파티 모집 중일 때는 예약금 지불 불가
        if (party.getStatus() == PartyStatus.RECRUITING) {
            throw new BusinessException(ErrorCode.CANNOT_PAY_FEE_RECRUITING);
        }

        PartyUser partyUser = partyService.findPartyUser(userId, party);

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

        partyChatService.noticePaymentComplete(user, party);

        // 모두 결제 했으면, 예약 자동 생성
        boolean allPaid = party.getPartyUsers().stream().allMatch(PartyUser::isPaymentCompleted);
        if (allPaid) {
            User host = partyService.findPartyHost(party);
            reservationCommandService.createPartyReservation(party, host);
            partyChatService.noticeReservationComplete(host, party);
        }
    }
}
