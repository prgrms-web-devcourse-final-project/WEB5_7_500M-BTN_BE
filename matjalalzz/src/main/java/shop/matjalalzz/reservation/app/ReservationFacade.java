package shop.matjalalzz.reservation.app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse.ReservationContent;
import shop.matjalalzz.reservation.dto.ReservationSummaryDto;
import shop.matjalalzz.reservation.dto.projection.CancelReservationProjection;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationQueryService query;
    private final ReservationCommandService command;
    private final ShopService shopService;
    private final UserService userService;
    private final PartyService partyService;

    // -------------------- Query path --------------------

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(
        Long shopId, ReservationStatus status, Long ownerId, Long cursor, int size
    ) {
        if (shopId != null) {
            Shop shop = shopService.shopFind(shopId);
            if (!shop.getUser().getId().equals(ownerId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            }
            var slice = query.findByShopIdWithFilterAndCursor(shopId, status, cursor, size);
            return toReservationListResponse(slice.getContent(), slice.hasNext());
        }

        List<Shop> shops = shopService.findByOwnerId(ownerId);
        if (shops == null || shops.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }

        List<Long> shopIds = shops.stream().map(Shop::getId).toList();
        var slice = query.findByShopIdsWithFilterAndCursor(shopIds, status, cursor, size);
        return toReservationListResponse(slice.getContent(), slice.hasNext());
    }

    @Transactional(readOnly = true)
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
        List<ReservationSummaryDto> rows =
            query.findSummariesByOwnerWithCursor(ownerId, status, cursor, sizePlusOne);

        boolean hasNext = rows.size() > size;
        if (hasNext) {
            rows = rows.subList(0, size);
        }
        Long nextCursor = hasNext ? rows.get(rows.size() - 1).reservationId() : null;

        List<ReservationContent> content = ReservationMapper.toReservationProjectionContent(rows);
        return ReservationMapper.toReservationListResponse(content, nextCursor);
    }

    @Transactional(readOnly = true)
    public MyReservationPageResponse findMyReservationPage(Long userId, Long cursor, int size) {
        var slice = query.findMyReservations(userId, cursor, size); // Slice<MyReservationResponse>
        Long nextCursor = slice.hasNext() ? slice.getContent().getLast().reservationId() : null;
        return ReservationMapper.toMyReservationPageResponse(nextCursor, slice);
    }

    @Transactional
    public CreateReservationResponse createReservation(Long userId, Long shopId,
        CreateReservationRequest request) {
        Shop shop = shopService.shopFind(shopId);
        User user = userService.getUserById(userId);

        int reservationFee = shop.getReservationFee() * request.headCount();
        if (user.getPoint() < reservationFee) {
            throw new BusinessException(ErrorCode.LACK_OF_BALANCE);
        }

        LocalDateTime reservedAt = LocalDateTime.of(
            LocalDate.parse(request.date()), LocalTime.parse(request.time())
        );
        Reservation reservation = ReservationMapper.toEntity(request, reservedAt, shop, user);

        Reservation saved = command.save(reservation);
        user.decreasePoint(reservationFee);

        return ReservationMapper.toCreateReservationResponse(saved);
    }

    @Transactional
    public void confirmReservation(Long reservationId, Long ownerId) {
        Reservation r = query.getReservationById(reservationId);
        if (!r.getShop().getUser().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }
        command.changeStatus(r, ReservationStatus.CONFIRMED);
    }

    @Transactional
    public void refuseReservation(Long reservationId, Long ownerId) {
        Reservation r = query.getReservationById(reservationId);
        if (!r.getShop().getUser().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }
        Party party = r.getParty();
        if (party != null) {
            refundPartyReservationFee(party);
            partyService.breakParty(party);
        } else {
            r.getUser().increasePoint(r.getReservationFee());
        }
        command.changeStatus(r, ReservationStatus.REFUSED);
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation r = query.getReservationById(reservationId);
        if (!r.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        if (r.getStatus() != ReservationStatus.CONFIRMED
            && r.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }
        if (r.getStatus() == ReservationStatus.CONFIRMED &&
            r.getReservedAt().isBefore(LocalDateTime.now().plusDays(1))) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_D_DAY);
        }

        Party party = r.getParty();
        if (party != null) {
            refundPartyReservationFee(party);
            partyService.breakParty(party);
        } else {
            r.getUser().increasePoint(r.getReservationFee());
        }
        command.changeStatus(r, ReservationStatus.CANCELLED);
    }

    @Transactional
    public void cancelReservationForWithdraw(User user) {
        List<CancelReservationProjection> reservations =
            query.findAllMyReservationByUserIdForWithdraw(user.getId());

        AtomicInteger total = new AtomicInteger();
        List<Long> ids = reservations.stream().map(row -> {
            total.addAndGet(row.getReservationFee());
            return row.getReservationId();
        }).toList();

        command.cancelReservations(ids);
        user.increasePoint(total.get());
    }

    @Transactional
    public int terminateExpiredReservations() {
        var threshold = LocalDateTime.now().minusDays(1);
        List<Reservation> list =
            query.findAllByStatusAndReservedAtBefore(ReservationStatus.CONFIRMED, threshold);

        for (Reservation r : list) {
            command.changeStatus(r, ReservationStatus.TERMINATED);
            if (r.getParty() != null) {
                partyService.terminateParty(r.getParty());
            }
            command.settleReservationFee(r.getShop().getId(), r.getReservationFee());
        }
        return list.size();
    }

    @Transactional
    public int refuseExpiredPendingReservations() {
        var threshold = LocalDateTime.now().minusHours(1);
        List<Reservation> list =
            query.findAllByStatusAndReservedAtBefore(ReservationStatus.PENDING, threshold);

        for (Reservation r : list) {
            if (r.getStatus() == ReservationStatus.PENDING) {
                Party party = r.getParty();
                if (party != null) {
                    refundPartyReservationFee(party);
                    partyService.breakParty(party);
                } else {
                    r.getUser().increasePoint(r.getReservationFee());
                }
                command.changeStatus(r, ReservationStatus.REFUSED);
            }
        }
        return list.size();
    }

    private ReservationListResponse toReservationListResponse(List<Reservation> reservations,
        boolean hasNext) {
        Long nextCursor = hasNext ? reservations.getLast().getId() : null;
        List<ReservationContent> content = ReservationMapper.toReservationContent(reservations);
        return ReservationMapper.toReservationListResponse(content, nextCursor);
    }


    public Reservation getReservationById(Long id) {
        Reservation reservation = query.getReservationById(id);
        return reservation;
    }

    @Transactional
    public void refundPartyReservationFee(Party party) {
        int fee = party.getShop().getReservationFee();
        command.refundPartyReservationFee(party.getId(), fee);
    }

    @Transactional
    public void createPartyReservation(Party party, User host) {
        Reservation reservation = ReservationMapper.toEntity(party, host);
        command.save(reservation);
    }
}
