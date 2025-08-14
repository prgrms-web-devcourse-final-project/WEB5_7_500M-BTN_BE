package shop.matjalalzz.reservation.dto;

import java.time.LocalDateTime;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public record ReservationSummaryDto(
    Long reservationId,
    String shopName,
    LocalDateTime reservedAt,
    int headCount,
    String phoneNumber,
    ReservationStatus status
) {

}
