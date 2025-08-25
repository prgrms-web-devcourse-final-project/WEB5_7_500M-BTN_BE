package shop.matjalalzz.reservation.dto.projection;

import java.time.LocalDateTime;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface MyReservationProjection {
    long getReservationId();
    String getShopName();
    String getName();
    LocalDateTime getReservedAt();
    int getHeadCount();
    int getReservationFee();
    ReservationStatus getStatus();
}
