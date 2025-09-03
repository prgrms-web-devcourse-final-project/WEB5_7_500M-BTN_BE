package shop.matjalalzz.reservation.app;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.projection.CancelReservationProjection;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.shop.app.ShopFacade;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandService {

    private final ReservationService reservationService;
    private final ShopFacade shopFacade;
    private final UserService userService;
    private final PartyService partyService;

    public CreateReservationResponse createReservation(
        Long userId, Long shopId, CreateReservationRequest request
    ) {
        Shop reservationShop = shopFacade.findShop(shopId);
        User reservationUser = userService.getUserById(userId);

        int reservationFee = reservationShop.getReservationFee() * request.headCount();
        if (reservationUser.getPoint() < reservationFee) {
            throw new BusinessException(ErrorCode.LACK_OF_BALANCE);
        }

        LocalDateTime reservedAt = LocalDateTime.parse(request.date() + "T" + request.time());
        Reservation reservation = ReservationMapper.toEntity(
            request, reservedAt, reservationShop, reservationUser
        );

        Reservation saved = reservationService.saveReservation(reservation);
        reservationUser.decreasePoint(reservationFee);

        return ReservationMapper.toCreateReservationResponse(saved);
    }

    public void confirmReservation(Long reservationId, Long ownerId) {
        Reservation r = validateOwnerPermissionAndPending(reservationId, ownerId);
        r.changeStatus(ReservationStatus.CONFIRMED);
    }

    public void refuseReservation(Long reservationId, Long ownerId) {
        Reservation r = validateOwnerPermissionAndPending(reservationId, ownerId);
        refuseReservationInternal(r);
    }

    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        log.info("[CANCEL] reservationId={}, reservationUserId={}, callerUserId={}",
            reservationId, reservation.getUser().getId(), userId);

        if (!reservation.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        ReservationStatus status = reservation.getStatus();
        if (status != ReservationStatus.CONFIRMED && status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

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

    public void cancelReservationForWithdraw(User user) {
        List<CancelReservationProjection> reservations =
            reservationService.findAllMyReservationByUserIdForWithdraw(user.getId());

        AtomicInteger totalReservationFee = new AtomicInteger();
        List<Long> reservationIds = reservations.stream().map(r -> {
            totalReservationFee.addAndGet(r.getReservationFee());
            return r.getReservationId();
        }).toList();

        reservationService.cancelReservations(reservationIds);
        user.increasePoint(totalReservationFee.get());
    }

    public int terminateExpiredReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        List<Reservation> toTerminate = reservationService.findAllByStatusAndReservedAtBefore(
            ReservationStatus.CONFIRMED, threshold
        );

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

    public int refuseExpiredPendingReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        List<Reservation> toRefuse = reservationService
            .findAllByStatusAndReservedAtBefore(ReservationStatus.PENDING, threshold);

        for (Reservation r : toRefuse) {
            refuseReservationInternal(r);
        }
        return toRefuse.size();
    }

    // --- private helpers ---
    private Reservation validateOwnerPermissionAndPending(Long reservationId, Long ownerId) {
        Reservation reservation = reservationService.getReservationById(reservationId);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }
        if (!reservation.getShop().getUser().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return reservation;
    }

    private void refuseReservationInternal(Reservation reservation) {
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
