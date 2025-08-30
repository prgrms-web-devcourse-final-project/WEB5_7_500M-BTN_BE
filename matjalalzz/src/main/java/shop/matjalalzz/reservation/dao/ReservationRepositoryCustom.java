package shop.matjalalzz.reservation.dao;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface ReservationRepositoryCustom {
    Slice<Reservation> findByShopIdWithFilterAndCursorQdsl(Long shopId, ReservationStatus status, Long cursor, Pageable pageable);
    Slice<Reservation> findByShopIdsWithFilterAndCursorQdsl(List<Long> shopIds, ReservationStatus status, Long cursor, Pageable pageable);
    Slice<MyReservationResponse> findByUserIdAndCursorQdsl(Long userId, Long cursor, Pageable pageable);
    List<Reservation> findAllByStatusAndReservedAtBeforeQdsl(ReservationStatus status, LocalDateTime threshold);

}
