package shop.matjalalzz.reservation.dto;

import java.time.LocalDateTime;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface MyReservationView {
    long getReservationId();
    String getShopName();
    String getName();
    LocalDateTime getReservedAt();
    int getHeadCount();
    int getReservationFee();
    ReservationStatus getStatus();
}
