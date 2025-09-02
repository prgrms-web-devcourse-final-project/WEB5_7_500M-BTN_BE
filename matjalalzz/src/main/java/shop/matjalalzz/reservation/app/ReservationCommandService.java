package shop.matjalalzz.reservation.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;

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

    public void refundPartyReservationFee(Long partyId, int refundAmount) {
        reservationRepository.refundPartyReservationFee(partyId, refundAmount);
    }
}
