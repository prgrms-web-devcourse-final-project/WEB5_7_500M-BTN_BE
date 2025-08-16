package shop.matjalalzz.reservation.app;

import static shop.matjalalzz.global.exception.domain.ErrorCode.ALREADY_PROCESSED;
import static shop.matjalalzz.global.exception.domain.ErrorCode.FORBIDDEN_ACCESS;
import static shop.matjalalzz.global.exception.domain.ErrorCode.SHOP_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse.ReservationContent;
import shop.matjalalzz.reservation.dto.ReservationSummaryDto;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final ShopService shopService;
    private final UserService userService;
    private final PartyService partyService;

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(Long shopId, ReservationStatus status,
        Long ownerId, Long cursor, int size) {

        Pageable pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "id"));

        if (shopId != null) {
            Shop shop = shopService.shopFind(shopId); // 검증용

            if (!shop.getUser().getId().equals(ownerId)) {
                throw new BusinessException(FORBIDDEN_ACCESS);
            }

            Slice<Reservation> slice = reservationService.findByShopIdWithFilterAndCursor(
                shopId, status, cursor, pageable
            );

//            Slice<Reservation> slice = reservationService.findByShopIdWithFilterAndCursorQdsl(
//                shopId, status, cursor, pageable
//            );

            return reservationResponse(slice);
        }

        List<Shop> shops = shopService.findByOwnerId(ownerId);
        if (shops == null) {
            throw new BusinessException(SHOP_NOT_FOUND);
        }

        List<Long> shopIds = shops.stream()
            .map(Shop::getId)
            .toList();

        Slice<Reservation> slice = reservationService.findByShopIdsWithFilterAndCursor(
            shopIds, status, cursor, pageable
        );

//        Slice<Reservation> slice = reservationService.findByShopIdsWithFilterAndCursorQdsl(
//            shopIds, status, cursor, pageable
//        );

        return reservationResponse(slice);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservationsProjection(Long ownerId, ReservationStatus status, Long cursor, int size) {
        int sizePlusOne = size + 1;
        Pageable pageable = PageRequest.of(0, sizePlusOne, Sort.by(Sort.Direction.DESC, "id"));

        // A안: 오너 기준 (ShopService 호출 제거)
        List<ReservationSummaryDto> rows =
            reservationService.findSummariesByOwnerWithCursor(ownerId, status, cursor, pageable);

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);
        Long nextCursor = hasNext ? rows.get(rows.size() - 1).reservationId() : null;

        List<ReservationListResponse.ReservationContent> content =
            ReservationMapper.toReservationProjectionContent(rows);

        return ReservationMapper.toReservationListResponse(content, nextCursor);
    }

    private ReservationListResponse reservationResponse(Slice<Reservation> slice) {
        List<Reservation> reservations = slice.getContent();

        Long nextCursor = slice.hasNext()
            ? reservations.getLast().getId()
            : null;

        List<ReservationContent> content =
            ReservationMapper.toReservationContent(reservations);

        return ReservationMapper.toReservationListResponse(content, nextCursor);
    }

    @Transactional(readOnly = true)
    public MyReservationPageResponse findMyReservationPage(Long userId, Long cursor, int size) {
//        Slice<MyReservationResponse> reservations = reservationService.findByUserIdAndCursor(
//            userId, cursor,
//            PageRequest.of(0, size));

        Slice<MyReservationResponse> reservations = reservationService.findByUserIdAndCursorQdsl(
            userId, cursor,
            PageRequest.of(0, size));

        Long nextCursor = null;
        if (reservations.hasNext()) {
            nextCursor = reservations.getContent().getLast().reservationId();
        }

        return ReservationMapper.toMyReservationPageResponse(nextCursor, reservations);
    }

    @Transactional
    public CreateReservationResponse createReservation(Long userId, Long shopId,
        CreateReservationRequest request) {

        Shop reservationShop = shopService.shopFind(shopId);
        User reservationUser = userService.getUserById(userId);
        int reservationFee = reservationShop.getReservationFee() * request.headCount();

        if (reservationUser.getPoint() < reservationFee) {
            throw new BusinessException(ErrorCode.LACK_OF_BALANCE);
        }

        LocalDateTime reservedAt = LocalDateTime.parse(request.date() + "T" + request.time());

        Reservation reservation = ReservationMapper.toEntity(
            request,
            reservedAt,
            reservationShop,
            reservationUser
        );

        Reservation savedReservation = reservationService.saveReservation(reservation);

        reservationUser.decreasePoint(reservationFee);

        return ReservationMapper.toCreateReservationResponse(savedReservation);
    }

    @Transactional
    public void confirmReservation(Long reservationId, Long ownerId) {
        Reservation reservation = validateOwnerPermissionAndPending(reservationId, ownerId);

        reservation.changeStatus(ReservationStatus.CONFIRMED);
    }

    @Transactional
    public void refuseReservation(Long reservationId, Long ownerId) {
        Reservation reservation = validateOwnerPermissionAndPending(reservationId, ownerId);

        refuseReservation(reservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        log.info("[CANCEL] reservationId={}, reservationUserId={}, callerUserId={}",
            reservationId, reservation.getUser().getId(), userId);

        // 예약한 본인인지 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new BusinessException(FORBIDDEN_ACCESS);
        }

        ReservationStatus status = reservation.getStatus();

        // 예약 상태 확인
        if (status != ReservationStatus.CONFIRMED && status != ReservationStatus.PENDING) {
            throw new BusinessException(ALREADY_PROCESSED);
        }

        // 예약이 승인됐고, 예약일로부터 하루도 안남았으면, 취소 불가
        if (status == ReservationStatus.CONFIRMED &&
            reservation.getReservedAt().isBefore(LocalDateTime.now().plusDays(1))) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_D_DAY);
        }

        Party party = reservation.getParty();

        if (party != null) {
            reservationService.refundPartyReservationFee(party);
            partyService.breakParty(party);
        } else {
            reservation.getUser().increasePoint(reservation.getReservationFee());
        }

        reservation.changeStatus(ReservationStatus.CANCELLED);
    }

    @Transactional
    public void cancelReservationForWithdraw(User user) {
        // 회원 탈퇴시에 회원 단독으로 진행한 예약 중 취소 가능한 예약 조회
        List<Reservation> reservations = reservationService.findAllMyReservationByUserIdForWithdraw(
            user.getId());

        for (Reservation reservation : reservations) {
            // 예약금 환불
            reservation.getUser().increasePoint(reservation.getReservationFee());

            // 예약 취소
            reservation.changeStatus(ReservationStatus.CANCELLED);
        }
    }

    @Transactional
    public int terminateExpiredReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);

        List<Reservation> toTerminate = reservationService.findAllByStatusAndReservedAtBefore(
            ReservationStatus.CONFIRMED, threshold
        );

//        List<Reservation> toTerminate = reservationService
//            .findAllByStatusAndReservedAtBeforeQdsl(
//                ReservationStatus.CONFIRMED,
//                threshold
//            );

        for (Reservation r : toTerminate) {
            r.changeStatus(ReservationStatus.TERMINATED);

            Party party = r.getParty();

            if (party != null) {
                partyService.terminateParty(party);
            }

            // 예약금 정산
            reservationService.settleReservationFee(r.getShop().getId(), r.getReservationFee());
        }

        return toTerminate.size();
    }

    @Transactional
    public int refuseExpiredPendingReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);

        List<Reservation> toRefuse = reservationService
            .findAllByStatusAndReservedAtBefore(ReservationStatus.PENDING, threshold);

        for (Reservation r : toRefuse) {
            refuseReservation(r);
        }

        return toRefuse.size();
    }

    private Reservation validateOwnerPermissionAndPending(Long reservationId,
        Long ownerId) {
        Reservation reservation = reservationService.getReservationById(reservationId);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ALREADY_PROCESSED);
        }

        if (!reservation.getShop().getUser().getId().equals(ownerId)) {
            throw new BusinessException(FORBIDDEN_ACCESS);
        }

        return reservation;
    }

    private void refuseReservation(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        reservation.changeStatus(ReservationStatus.REFUSED);

        Party party = reservation.getParty();

        if (party != null) {
            reservationService.refundPartyReservationFee(party);
            partyService.breakParty(party);

        } else {
            reservation.getUser().increasePoint(reservation.getReservationFee());
        }
    }
}
