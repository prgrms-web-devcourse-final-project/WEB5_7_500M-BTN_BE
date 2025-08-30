package shop.matjalalzz.reservation.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.reservation.app.command.ReservationCommandService;
import shop.matjalalzz.reservation.app.query.ReservationQueryService;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationQueryService queryService;
    private final ReservationCommandService commandService;

    // --- Query path ---
    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(
        Long shopId, ReservationStatus status, Long ownerId, Long cursor, int size
    ) {
        return queryService.getReservations(shopId, status, ownerId, cursor, size);
    }

    @Transactional(readOnly = true)
    public ReservationListResponse getReservationsProjection(
        Long shopId, ReservationStatus status, Long ownerId, Long cursor, int size
    ) {
        return queryService.getReservationsProjection(shopId, status, ownerId, cursor, size);
    }

    @Transactional(readOnly = true)
    public MyReservationPageResponse findMyReservationPage(Long userId, Long cursor, int size) {
        return queryService.findMyReservationPage(userId, cursor, size);
    }

    // --- Command path ---
    @Transactional
    public CreateReservationResponse createReservation(Long userId, Long shopId,
        CreateReservationRequest request) {
        return commandService.createReservation(userId, shopId, request);
    }

    @Transactional
    public void confirmReservation(Long reservationId, Long ownerId) {
        commandService.confirmReservation(reservationId, ownerId);
    }

    @Transactional
    public void refuseReservation(Long reservationId, Long ownerId) {
        commandService.refuseReservation(reservationId, ownerId);
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        commandService.cancelReservation(reservationId, userId);
    }

    @Transactional
    public void cancelReservationForWithdraw(User user) {
        commandService.cancelReservationForWithdraw(user);
    }

    @Transactional
    public int terminateExpiredReservations() {
        return commandService.terminateExpiredReservations();
    }

    @Transactional
    public int refuseExpiredPendingReservations() {
        return commandService.refuseExpiredPendingReservations();
    }
}
