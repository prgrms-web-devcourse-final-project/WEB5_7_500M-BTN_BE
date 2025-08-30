package shop.matjalalzz.reservation.dto.projection;

import java.time.LocalDateTime;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface ReservationSummaryProjection {
    Long getReservationId();
    String getShopName();
    LocalDateTime getReservedAt();
    Integer getHeadCount();
    String getPhoneNumber();
    ReservationStatus getStatus();
}