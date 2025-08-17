package shop.matjalalzz.reservation.dao;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.reservation.entity.QReservation;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory query;
    private static final QReservation r = QReservation.reservation;

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

