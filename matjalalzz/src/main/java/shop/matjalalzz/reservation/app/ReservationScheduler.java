package shop.matjalalzz.reservation.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    @Scheduled(cron = "0 0 * * * *")
    public void refusePendingReservations() {
        int count = reservationService.refuseExpiredPendingReservations();
        log.info("[PENDING -> REFUSED] 처리된 예약 수 = {}", count);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void terminateConfirmedReservations() {
        int count = reservationService.terminateExpiredReservations();
        log.info("[CONFIRMED -> TERMINATED] 처리된 예약 수 = {}", count);
    }
}