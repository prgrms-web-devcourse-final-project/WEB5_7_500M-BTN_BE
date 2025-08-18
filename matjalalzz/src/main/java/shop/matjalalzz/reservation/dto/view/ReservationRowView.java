package shop.matjalalzz.reservation.dto.view;

import java.time.LocalDateTime;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface ReservationRowView {
    Long getReservationId();
    Long getShopId();
    String getShopName();
    Long getUserId();
    String getUserName();
    LocalDateTime getReservedAt();
    Integer getHeadCount();
    Integer getReservationFee();
    ReservationStatus getStatus();
}