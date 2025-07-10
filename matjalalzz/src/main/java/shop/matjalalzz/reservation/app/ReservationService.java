package shop.matjalalzz.reservation.app;

import static shop.matjalalzz.global.exception.domain.ErrorCode.INVALID_RESERVATION_STATUS;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.global.exception.domain.ErrorCode;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

//    private final ClientService clientService;
//    private final ShopService shopService;

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

    public CreateReservationResponse createReservation(Long shopId, CreateReservationRequest request) {
        return CreateReservationResponse.builder()
            .reservationId(1L)
            .shopName("Mock 식당")
            .dateTime(request.date() + "T" + request.time())
            .headCount(request.headCount())
            .status("PENDING")
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

