package shop.matjalalzz.reservation.dto.view;

import java.time.LocalDateTime;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface ReservationSummaryView {
    Long getReservationId();
    String getShopName();
    LocalDateTime getReservedAt();
    Integer getHeadCount();
    String getPhoneNumber();
    ReservationStatus getStatus();
}