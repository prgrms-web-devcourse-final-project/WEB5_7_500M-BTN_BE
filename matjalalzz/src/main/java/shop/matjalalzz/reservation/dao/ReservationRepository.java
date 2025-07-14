package shop.matjalalzz.reservation.dao;

import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.review.dto.MyReviewResponse;

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

    @Query("""
        select new shop.matjalalzz.reservation.dto.MyReservationResponse(
                r.id, s.name, u.name, r.reservedAt, r.headCount, r.reservationFee, r.status)
        from Reservation r
            join r.shop  s
            join r.user  u
        where (:cursor is null or r.id < :cursor)
            and (
                r.user.id = :userId
                or exists (
                    select 1 from PartyUser pu
                    where pu.party = r.party
                        and pu.user.id = :userId
                )
            )
        order by r.id desc
        """)
    Slice<MyReservationResponse> findByUserIdAndCursor(
        @Param("userId") Long userId,
        @Param("cursor") Long cursor,
        Pageable pageable);
}
