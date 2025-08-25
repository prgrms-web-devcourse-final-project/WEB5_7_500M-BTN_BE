package shop.matjalalzz.reservation.app;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public void createPartyReservation(Party party, User host) {
        Reservation reservation = ReservationMapper.toEntity(party, host);
        reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation saveReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void refundPartyReservationFee(Party party) {
        int fee = party.getShop().getReservationFee();
        reservationRepository.refundPartyReservationFee(party.getId(), fee);
    }

    @Transactional
    public void settleReservationFee(Long shopId, int reservationFee) {
        reservationRepository.settleReservationFee(shopId, reservationFee);
    }

    @Transactional(readOnly = true)
    public Slice<MyReservationResponse> findByUserIdAndCursor(
        Long userId, Long cursor, Pageable pageable) {
        return reservationRepository.findByUserIdAndCursor(userId, cursor, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Reservation> findByShopIdsWithFilterAndCursor(
        List<Long> shopIds, ReservationStatus status, Long cursor, Pageable pageable) {
        return reservationRepository.findByShopIdsWithFilterAndCursor(
            shopIds, status, cursor, pageable
        );
    }

    @Transactional(readOnly = true)
    public Slice<Reservation> findByShopIdWithFilterAndCursor(
        Long shopId, ReservationStatus status, Long cursor, Pageable pageable) {
        return reservationRepository.findByShopIdWithFilterAndCursor(
            shopId, status, cursor, pageable
        );
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllMyReservationByUserIdForWithdraw(Long userId) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(1);

        // 회원 단독으로 진행한 예약 중 대기 상태이거나 예약일로부터 하루 이상 남은 예약 조회
        return reservationRepository.findAllMyReservationByUserIdForWithdraw(userId, threshold);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByStatusAndReservedAtBefore(ReservationStatus status,
        LocalDateTime threshold) {
        return reservationRepository.findAllByStatusAndReservedAtBefore(status, threshold);
    }

    @Transactional(readOnly = true)
    public Reservation findByPartyId(Long partyId) {
        return reservationRepository.findByPartyId(partyId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Reservation> findAllByPartyIds(List<Long> partyIds) {

        //key는 partyId, value는 null인 map으로 초기화
        Map<Long, Reservation> byPartyId = new HashMap<>();
        partyIds.stream().distinct().forEach(id -> byPartyId.put(id, null));

        List<Reservation> reservations = reservationRepository.findAllByPartyIds(partyIds);
        // 연동된 예약이 있는 경우 map 값 덮어쓰기
        for (Reservation reservation : reservations) {
            Long partyId = reservation.getParty().getId();
            byPartyId.put(partyId, reservation);
        }
        return byPartyId;
    }
}

