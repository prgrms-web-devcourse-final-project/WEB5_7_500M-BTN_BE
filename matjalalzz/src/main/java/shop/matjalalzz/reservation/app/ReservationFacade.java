package shop.matjalalzz.reservation.app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse.ReservationContent;
import shop.matjalalzz.reservation.dto.projection.ReservationSummaryProjection;
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

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final ShopService shopService;
    private final UserService userService;
    private final PartyService partyService;

    @Transactional(readOnly = true)
    public ReservationListResponse getReservationsProjection(
        Long shopId, ReservationStatus status, Long ownerId, Long cursor, int size
    ) {

        if(shopId != null){
            shopService.validShop(shopId, ownerId);
        }

        int sizePlusOne = size + 1;
        Slice<ReservationSummaryProjection> slice =
            reservationQueryService.findSummariesByOwnerWithCursor(ownerId, status, cursor,
                sizePlusOne);

        Long nextCursor = slice.hasNext() ? slice.getContent().getLast().getReservationId() : null;

        List<ReservationContent> content = ReservationMapper.toReservationProjectionContent(slice);
        return ReservationMapper.toReservationListResponse(content, nextCursor);
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

        Reservation saved = reservationCommandService.save(reservation);
        user.decreasePoint(reservationFee);

        return ReservationMapper.toCreateReservationResponse(saved);
    }

    @Transactional
    public void confirmReservation(Long reservationId, Long ownerId) {
        Reservation r = validShopAndOwner(reservationId, ownerId);
        reservationCommandService.changeStatus(r, ReservationStatus.CONFIRMED);
    }

    @Transactional
    public void refuseReservation(Long reservationId, Long ownerId) {
        Reservation r = validShopAndOwner(reservationId, ownerId);
        Party party = r.getParty();
        if (party != null) {
            refundPartyReservationFee(party);
            partyService.breakParty(party);
        } else {
            r.getUser().increasePoint(r.getReservationFee());
        }
        reservationCommandService.changeStatus(r, ReservationStatus.REFUSED);
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation r = reservationQueryService.getByIdWithShopAndOwner(reservationId);
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
        reservationCommandService.changeStatus(r, ReservationStatus.CANCELLED);
    }

    @Transactional
    public int terminateExpiredReservations() {
        var threshold = LocalDateTime.now().minusDays(1);
        List<Reservation> list =
            reservationQueryService.findAllByStatusAndReservedAtBefore(ReservationStatus.CONFIRMED,
                threshold);

        for (Reservation r : list) {
            reservationCommandService.changeStatus(r, ReservationStatus.TERMINATED);
            if (r.getParty() != null) {
                partyService.terminateParty(r.getParty());
            }
            reservationCommandService.settleReservationFee(r.getShop().getId(),
                r.getReservationFee());
        }
        return list.size();
    }

    @Transactional
    public int refuseExpiredPendingReservations() {
        var threshold = LocalDateTime.now().minusHours(1);
        List<Reservation> list =
            reservationQueryService.findAllByStatusAndReservedAtBefore(ReservationStatus.PENDING,
                threshold);

        for (Reservation r : list) {
            if (r.getStatus() == ReservationStatus.PENDING) {
                Party party = r.getParty();
                if (party != null) {
                    refundPartyReservationFee(party);
                    partyService.breakParty(party);
                } else {
                    r.getUser().increasePoint(r.getReservationFee());
                }
                reservationCommandService.changeStatus(r, ReservationStatus.REFUSED);
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

    @Transactional
    public void refundPartyReservationFee(Party party) {
        reservationCommandService.refundPartyReservationFee(party);
    }

    private Reservation validShopAndOwner(Long reservationId, Long ownerId) {
        Reservation r = reservationQueryService.getByIdWithShopAndOwner(reservationId);
        if (!r.getShop().getUser().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }
        return r;
    }

}
