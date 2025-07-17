package shop.matjalalzz.reservation.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationTerminator {

    private final ReservationService reservationService;

    @Scheduled(cron = "0 0 * * * *")
    public void terminateReservations() {
        int count = reservationService.terminateExpiredReservations();
        log.info("[TERMINATE 배치] 처리된 예약 수 = {}", count);
    }
}