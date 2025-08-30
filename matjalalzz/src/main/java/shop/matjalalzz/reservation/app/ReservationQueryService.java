package shop.matjalalzz.reservation.app.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse.ReservationContent;
import shop.matjalalzz.reservation.dto.ReservationSummaryDto;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationService reservationService;
    private final ShopService shopService;

    public ReservationListResponse getReservations(
        Long shopId, ReservationStatus status, Long ownerId, Long cursor, int size
    ) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));

        if (shopId != null) {
            Shop shop = shopService.shopFind(shopId); // 검증
            if (!shop.getUser().getId().equals(ownerId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            }
            Slice<Reservation> slice = reservationService.findByShopIdWithFilterAndCursor(
                shopId, status, cursor, pageable
            );
            return toReservationListResponse(slice);
        }

        List<Shop> shops = shopService.findByOwnerId(ownerId);
        if (shops == null) throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);

        List<Long> shopIds = shops.stream().map(Shop::getId).toList();
        Slice<Reservation> slice = reservationService.findByShopIdsWithFilterAndCursor(
            shopIds, status, cursor, pageable
        );
        return toReservationListResponse(slice);
    }

    public ReservationListResponse getReservationsProjection(
        Long shopId, ReservationStatus status, Long ownerId, Long cursor, int size
    ) {
        if (shopId != null) {
            Shop shop = shopService.shopFind(shopId);
            if (!shop.getUser().getId().equals(ownerId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            }
        }

        int sizePlusOne = size + 1;
        Pageable pageable = PageRequest.of(0, sizePlusOne, Sort.by(Sort.Direction.DESC, "id"));

        List<ReservationSummaryDto> rows =
            reservationService.findSummariesByOwnerWithCursor(ownerId, status, cursor, pageable);

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);
        Long nextCursor = hasNext ? rows.get(rows.size() - 1).reservationId() : null;

        List<ReservationContent> content = ReservationMapper.toReservationProjectionContent(rows);
        return ReservationMapper.toReservationListResponse(content, nextCursor);
    }

    public MyReservationPageResponse findMyReservationPage(Long userId, Long cursor, int size) {
        Slice<MyReservationResponse> reservations = reservationService.findByUserIdAndCursor(
            userId, cursor, PageRequest.of(0, size));

        Long nextCursor = reservations.hasNext()
            ? reservations.getContent().getLast().reservationId()
            : null;

        return ReservationMapper.toMyReservationPageResponse(nextCursor, reservations);
    }

    // --- helpers ---
    private ReservationListResponse toReservationListResponse(Slice<Reservation> slice) {
        List<Reservation> list = slice.getContent();
        Long nextCursor = slice.hasNext() ? list.getLast().getId() : null;
        List<ReservationContent> content = ReservationMapper.toReservationContent(list);
        return ReservationMapper.toReservationListResponse(content, nextCursor);
    }
}
