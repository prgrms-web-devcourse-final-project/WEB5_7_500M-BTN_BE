package shop.matjalalzz.reservation.dao;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.shop.id = :shopId AND r.reservedAt = :reservedAt")
    Optional<Reservation> findWithLockByShopIdAndReservedAt(@Param("shopId") Long shopId,
        @Param("reservedAt") LocalDateTime reservedAt);



}
