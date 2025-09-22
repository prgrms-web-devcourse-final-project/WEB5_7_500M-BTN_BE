package shop.matjalalzz.reservation.app;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.projection.CancelReservationProjection;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryService reservationQueryService;

    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public void changeStatus(Reservation reservation, ReservationStatus status) {
        reservation.changeStatus(status);
    }

    public void cancelReservations(List<Long> reservationIds) {
        reservationRepository.cancelReservationByIds(reservationIds);
    }

    public void settleReservationFee(Long shopId, int reservationFee) {
        reservationRepository.settleReservationFee(shopId, reservationFee);
    }

    public void refundPartyReservationFee(Party party) {
        int fee = party.getShop().getReservationFee();
        reservationRepository.refundPartyReservationFee(party.getId(), fee);
    }

    public void createPartyReservation(Party party, User host){
        Reservation reservation = ReservationMapper.toEntity(party, host);
        save(reservation);
    }

    public void cancelReservationForWithdraw(User user) {
        List<CancelReservationProjection> reservations =
            reservationQueryService.findAllMyReservationByUserIdForWithdraw(user.getId());

        AtomicInteger total = new AtomicInteger();
        List<Long> ids = reservations.stream().map(row -> {
            total.addAndGet(row.getReservationFee());
            return row.getReservationId();
        }).toList();

        cancelReservations(ids);
        user.increasePoint(total.get());
    }
}
