package shop.matjalalzz.reservation.dao;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.entity.QReservation;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.entity.QShop;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory query;
    private static final QReservation r = QReservation.reservation;
    private static final QShop s = QShop.shop;

    @Override
    public Slice<Reservation> findByShopIdWithFilterAndCursorQdsl(Long shopId, ReservationStatus status, Long cursor, Pageable pageable) {
        var list = query.selectFrom(r)
            .where(r.shop.id.eq(shopId), eqStatus(status), ltCursor(cursor))
            .orderBy(r.id.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();
        return toSlice(list, pageable.getPageSize());
    }

    @Override
    public Slice<Reservation> findByShopIdsWithFilterAndCursorQdsl(List<Long> shopIds, ReservationStatus status, Long cursor, Pageable pageable) {
        if (shopIds == null || shopIds.isEmpty()) return new SliceImpl<>(List.of(), pageable, false);
        var list = query.selectFrom(r)
            .where(r.shop.id.in(shopIds), eqStatus(status), ltCursor(cursor))
            .orderBy(r.id.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();
        return toSlice(list, pageable.getPageSize());
    }

    @Override
    public Slice<MyReservationResponse> findByUserIdAndCursorQdsl(Long userId, Long cursor, Pageable pageable) {
        var list = query
            .select(Projections.constructor(MyReservationResponse.class,
                r.id,
                s.shopName,
                r.reservedAt,
                r.headCount,
                r.reservationFee,
                r.status
            ))
            .from(r)
            .join(r.shop, s)
            .where(r.user.id.eq(userId), ltCursor(cursor))
            .orderBy(r.id.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();
        boolean hasNext = list.size() > pageable.getPageSize();
        if (hasNext) list = list.subList(0, pageable.getPageSize());
        return new SliceImpl<>(list, pageable, hasNext);
    }

    @Override
    public List<Reservation> findAllByStatusAndReservedAtBeforeQdsl(ReservationStatus status, LocalDateTime threshold) {
        return query.selectFrom(r)
            .where(r.status.eq(status), r.reservedAt.loe(threshold))
            .fetch();
    }

    /* helpers */
    private BooleanExpression eqStatus(ReservationStatus st) { return st == null ? null : r.status.eq(st); }
    private BooleanExpression ltCursor(Long cursor) { return cursor == null ? null : r.id.lt(cursor); }
    private static <T> Slice<T> toSlice(List<T> rows, int size) {
        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);
        return new SliceImpl<>(rows, PageRequest.of(0, size), hasNext);
    }
}

