package shop.matjalalzz.reservation.app;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.dto.projection.CancelReservationProjection;
import shop.matjalalzz.reservation.dto.projection.MyReservationProjection;
import shop.matjalalzz.reservation.dto.projection.ReservationSummaryProjection;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public Slice<ReservationSummaryProjection> findSummariesByOwnerWithCursor(
        Long ownerId, ReservationStatus status, Long cursor, int sizePlusOne
    ) {
        Pageable pageable = PageRequest.of(0, sizePlusOne, Sort.by(Sort.Direction.DESC, "id"));
        return reservationRepository.findSummariesByOwnerWithCursor(ownerId, status, cursor,
            pageable);
    }

    public Slice<MyReservationResponse> findMyReservations(Long userId, Long cursor, int size) {
        Slice<MyReservationProjection> views =
            reservationRepository.findByUserIdAndCursor(userId, cursor, PageRequest.of(0, size));
        return views.map(ReservationMapper::toMyReservationResponse);
    }

    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    public List<Reservation> findAllByStatusAndReservedAtBefore(
        ReservationStatus status, LocalDateTime threshold
    ) {
        return reservationRepository.findAllByStatusAndReservedAtBefore(status, threshold);
    }

    public List<CancelReservationProjection> findAllMyReservationByUserIdForWithdraw(Long userId) {
        // 기존 Service 로직과 동일: "하루 이상 남은 예약만" 보려면 기준시각 +1일
        LocalDateTime threshold = LocalDateTime.now().plusDays(1);
        return reservationRepository.findAllMyReservationByUserIdForWithdraw(userId, threshold);
    }

    public Reservation getByIdWithShopAndOwner(Long reservationId) {
        return reservationRepository.findByIdWithShopAndOwner(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    public MyReservationPageResponse findMyReservationPage(Long userId, Long cursor, int size) {
        Slice<MyReservationResponse> slice = findMyReservations(userId, cursor, size); // Slice<MyReservationResponse>
        Long nextCursor = slice.hasNext() ? slice.getContent().getLast().reservationId() : null;
        return ReservationMapper.toMyReservationPageResponse(nextCursor, slice);
    }

}
