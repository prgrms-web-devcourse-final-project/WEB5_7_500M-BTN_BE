package shop.matjalalzz.reservation.dao;

import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.shop.id = :shopId
          AND (:status IS NULL OR r.status = :status)
          AND (:cursor IS NULL OR r.id < :cursor)
        ORDER BY r.id DESC
        """)
    Slice<Reservation> findByShopIdWithFilterAndCursor(
        @Param("shopId") Long shopId,
        @Param("status") ReservationStatus status,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.shop.id = :shopId
          AND r.reservedAt = :reservedAt
        """)
    boolean existsByShopIdAndReservationAt(
        @Param("shopId") Long shopId,
        @Param("reservedAt") LocalDateTime reservedAt
    );

}
