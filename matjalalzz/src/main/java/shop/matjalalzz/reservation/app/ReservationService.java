package shop.matjalalzz.reservation.app;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.reservation.dto.projection.CancelReservationProjection;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;

    @Transactional
    public void refundPartyReservationFee(Party party) {
        int fee = party.getShop().getReservationFee();
        reservationCommandService.refundPartyReservationFee(party.getId(), fee);
    }

    @Transactional
    public void createPartyReservation(Party party, User host) {
        Reservation reservation = ReservationMapper.toEntity(party, host);
        reservationCommandService.save(reservation);
    }

    public Reservation getReservationById(Long id) {
        Reservation reservation = reservationQueryService.getReservationById(id);
        return reservation;
    }

    @Transactional
    public void cancelReservationForWithdraw(User user) {
        List<CancelReservationProjection> reservations =
            reservationQueryService.findAllMyReservationByUserIdForWithdraw(user.getId());

        AtomicInteger total = new AtomicInteger();
        List<Long> ids = reservations.stream().map(row -> {
            total.addAndGet(row.getReservationFee());
            return row.getReservationId();
        }).toList();

        reservationCommandService.cancelReservations(ids);
        user.increasePoint(total.get());
    }

    @Transactional(readOnly = true)
    public MyReservationPageResponse findMyReservationPage(Long userId, Long cursor, int size) {
        var slice = reservationQueryService.findMyReservations(userId, cursor, size); // Slice<MyReservationResponse>
        Long nextCursor = slice.hasNext() ? slice.getContent().getLast().reservationId() : null;
        return ReservationMapper.toMyReservationPageResponse(nextCursor, slice);
    }
}
