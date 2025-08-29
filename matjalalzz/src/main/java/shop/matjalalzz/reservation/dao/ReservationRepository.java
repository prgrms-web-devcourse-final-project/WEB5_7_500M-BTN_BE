package shop.matjalalzz.reservation.dao;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.reservation.dto.ReservationSummaryDto;
import shop.matjalalzz.reservation.dto.projection.CancelReservationProjection;
import shop.matjalalzz.reservation.dto.projection.MyReservationProjection;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationSummaryDto;
import shop.matjalalzz.reservation.dto.projection.ReservationSummaryProjection;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.shop s
        JOIN FETCH r.user u
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
        SELECT r FROM Reservation r
        JOIN FETCH r.shop s
        JOIN FETCH r.user u
        WHERE r.shop.id IN :shopIds
          AND (:status IS NULL OR r.status = :status)
          AND (:cursor IS NULL OR r.id < :cursor)
        ORDER BY r.id DESC
        """)
    Slice<Reservation> findByShopIdsWithFilterAndCursor(
        @Param("shopIds") List<Long> shopIds,
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

    // 회원이 진행한 예약과 회원이 속한 파티가 진행한 예약을 조회
    @Query("""
        SELECT r.id as reservationId, s.shopName as shopName, u.name as name,
               r.reservedAt as reservedAt, r.headCount as headCount,
               r.reservationFee as reservationFee, r.status as status
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
    Slice<MyReservationProjection> findByUserIdAndCursor(
        @Param("userId") Long userId,
        @Param("cursor") Long cursor,
        Pageable pageable);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.status = :status
          AND r.reservedAt <= :threshold
        """)
    List<Reservation> findAllByStatusAndReservedAtBefore(
        @Param("status") ReservationStatus status,
        @Param("threshold") LocalDateTime threshold);

    @Query("""
        SELECT r.id AS reservationId, r.reservationFee AS reservationFee
        FROM Reservation r
        WHERE r.user.id = :userId
            AND r.party IS NULL
            AND (
                r.status = "PENDING"
                OR (
                    r.status = "CONFIRMED"
                    AND r.reservedAt >= :threshold
                )
            )
        """)
    List<CancelReservationProjection> findAllMyReservationByUserIdForWithdraw(@Param("userId") Long userId,
        @Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("""
        update User u
        set u.point = u.point + :reservationFee, u.version = u.version + 1
        where u.id = (select s.user.id from Shop s where s.id = :shopId)
        """)
    void settleReservationFee(
        @Param("shopId") long shopId,
        @Param("reservationFee") int reservationFee);

    @Modifying
    @Query("""
        update User u
        set u.point = u.point + :refundAmount
        where u.id IN (
            select pu.user.id from PartyUser pu where pu.party.id = :partyId and pu.paymentCompleted = true
        )
        """)
    void refundPartyReservationFee(@Param("partyId") Long partyId,
        @Param("refundAmount") int refundAmount);

    @Query("""
        select new shop.matjalalzz.reservation.dto.ReservationSummaryDto(
        r.id, s.shopName, r.reservedAt, r.headCount, u.phoneNumber, r.status
        )
        from Reservation r
        join r.shop s
        join r.user u
        where r.deleted = false
        and s.user.id = :ownerId
        and (:status is null or r.status = :status)
        and (:cursor is null or r.id < :cursor)
        order by r.id desc
            """)
    List<ReservationSummaryDto> findSummariesByOwnerWithCursor(
        @Param("ownerId") Long ownerId,
        @Param("status") ReservationStatus status,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    @Modifying
    @Query("""
        update Reservation r
        set r.status = "CANCELLED"
        where r.id in :reservationIds
        """)
    void cancelReservationByIds(@Param("reservationIds") List<Long> reservationIds);


    // 사장 요약 리스트 → 인터페이스 프로젝션
    @Query("""
        select
            r.id as reservationId,
            s.shopName as shopName,
            r.reservedAt as reservedAt,
            r.headCount as headCount,
            u.phoneNumber as phoneNumber,
            r.status as status
        from Reservation r
            join r.shop s
            join r.user u
        where r.deleted = false
          and s.user.id = :ownerId
          and (:status is null or r.status = :status)
          and (:cursor is null or r.id < :cursor)
        order by r.id desc
        """)
    Slice<ReservationSummaryProjection> findOwnerSummariesWithCursor(
        @Param("ownerId") Long ownerId,
        @Param("status") ReservationStatus status,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

}
