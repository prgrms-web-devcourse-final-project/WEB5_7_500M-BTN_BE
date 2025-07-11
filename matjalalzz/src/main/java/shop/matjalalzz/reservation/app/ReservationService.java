package shop.matjalalzz.reservation.app;

import static shop.matjalalzz.global.exception.domain.ErrorCode.INVALID_RESERVATION_STATUS;
import static shop.matjalalzz.global.exception.domain.ErrorCode.PARTY_NOT_FOUND;
import static shop.matjalalzz.global.exception.domain.ErrorCode.SHOP_NOT_FOUND;
import static shop.matjalalzz.global.exception.domain.ErrorCode.USER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.util.AuditorAwareImpl;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final PartyRepository partyRepository;
    private final AuditorAwareImpl auditor;

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(Long shopId, String filter, Long cursor,
        int size) {
        ReservationStatus status = parseFilter(filter);

        // 데이터 모두 가져오고 limit 처리
        List<Reservation> allResults = reservationRepository.findByShopIdWithFilterAndCursor(shopId,
            status, cursor);

        boolean hasNext = allResults.size() > size;
        List<Reservation> limitedResults = hasNext ? allResults.subList(0, size) : allResults;

        Long nextCursor = hasNext ? limitedResults.get(size - 1).getId() : null;

        List<ReservationListResponse.ReservationSummary> content = limitedResults.stream()
            .map(res -> ReservationListResponse.ReservationSummary.builder()
                .reservationId(res.getId())
                .shopName("ShopService 연동") //TODO: ShopService 연동 예정
                .reservedAt(res.getReservedAt().toString())
                .headCount(res.getHeadCount())
                .phoneNumber("ClientService 연동") //TODO: ClientService 연동 예정
                .build())
            .toList();

        return ReservationListResponse.builder()
            .content(content)
            .nextCursor(nextCursor)
            .build();
    }

    @Transactional
    public CreateReservationResponse createReservation(Long shopId, Long partyId,
        CreateReservationRequest request) {
        LocalDateTime reservedAt = LocalDateTime.parse(request.date() + "T" + request.time());

        Long userId = auditor.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        User reservationUser = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        Shop reservationShop = shopRepository.findById(shopId)
            .orElseThrow(() -> new BusinessException(SHOP_NOT_FOUND));

        Party reservationParty = null;
        if (partyId != null) {
            reservationParty = partyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException(PARTY_NOT_FOUND));
        }

        // 중복 예약 검사
        if (reservationRepository.existsByShopIdAndReservationAt(shopId, reservedAt)) {
            throw new BusinessException(INVALID_RESERVATION_STATUS);
        }

        Reservation reservation = Reservation.builder()
            .headCount(request.headCount())
            .reservationFee(request.reservationFee())
            .reservedAt(reservedAt)
            .status(ReservationStatus.PENDING)
            .shop(reservationShop)
            .user(reservationUser)
            .party(reservationParty)
            .build();

        Reservation savedReservation = null;

        try {
            reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(INVALID_RESERVATION_STATUS);
        }


        return CreateReservationResponse.builder()
            .reservationId(savedReservation.getId())
            .shopName(savedReservation.getShop().getName())
            .dateTime(savedReservation.getReservedAt().toString())
            .headCount(savedReservation.getHeadCount())
            .status(savedReservation.getStatus().name())
            .build();
    }

    private ReservationStatus parseFilter(String filter) {
        if (filter == null || filter.equalsIgnoreCase("TOTAL")) {
            return null;
        }

        try {
            return ReservationStatus.valueOf(filter.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(INVALID_RESERVATION_STATUS);
        }
    }
}

