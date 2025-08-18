package shop.matjalalzz.reservation.dto.view;

import java.time.LocalDateTime;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface MyReservationView {
    Long getReservationId();
    String getShopName();
    String getName();
    LocalDateTime getReservedAt();
    Integer getHeadCount();
    Integer getReservationFee();
    ReservationStatus getStatus();
}