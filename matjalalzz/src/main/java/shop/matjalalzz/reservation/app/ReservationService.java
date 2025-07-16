package shop.matjalalzz.reservation.app;

import static shop.matjalalzz.global.exception.domain.ErrorCode.ALREADY_PROCESSED;
import static shop.matjalalzz.global.exception.domain.ErrorCode.DATA_NOT_FOUND;
import static shop.matjalalzz.global.exception.domain.ErrorCode.FORBIDDEN_ACCESS;
import static shop.matjalalzz.global.exception.domain.ErrorCode.INVALID_REQUEST_DATA;
import static shop.matjalalzz.global.exception.domain.ErrorCode.RESERVATION_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse.ReservationContent;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.reservation.mapper.ReservationMapper;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final ShopService shopService;
    private final UserService userService;
    private final PartyService partyService;

    @Transactional(readOnly = true)
    public ReservationListResponse getReservations(Long shopId, ReservationStatus status,
        Long cursor,
        int size) {

        Pageable pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "id"));

        Slice<Reservation> slice = reservationRepository.findByShopIdWithFilterAndCursor(
            shopId, status, cursor, pageable
        );

        List<Reservation> reservations = slice.getContent();

        Long nextCursor =
            slice.hasNext() ? reservations.get(reservations.size() - 1).getId() : null;

        List<ReservationContent> content =
            ReservationMapper.toReservationContent(reservations);

        return ReservationMapper.toReservationListResponse(content, nextCursor);
    }

    @Transactional(readOnly = true)
    public MyReservationPageResponse findMyReservationPage(Long userId, Long cursor, int size) {
        Slice<MyReservationResponse> reservations = reservationRepository.findByUserIdAndCursor(
            userId, cursor,
            PageRequest.of(0, size));

        Long nextCursor = null;
        if (reservations.hasNext()) {
            nextCursor = reservations.getContent().getLast().reservationId();
        }

        return ReservationMapper.toMyReservationPageResponse(nextCursor, reservations);
    }

    @Transactional
    public CreateReservationResponse createReservation(Long userId, Long shopId, Long partyId,
        CreateReservationRequest request) {

        LocalDateTime reservedAt = LocalDateTime.parse(request.date() + "T" + request.time());

        User reservationUser = userService.getUserById(userId);

        Shop reservationShop = shopService.shopFind(shopId);

        Party reservationParty = null;
        if (partyId != null) {
            reservationParty = partyService.findById(partyId);
        }

        Reservation reservation = ReservationMapper.toEntity(
            request,
            reservedAt,
            reservationShop,
            reservationUser,
            reservationParty
        );

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationMapper.toCreateReservationResponse(savedReservation);
    }

    @Transactional
    public void confirmReservation(Long shopId, Long reservationId, Long ownerId) {
        Reservation reservation = validateOwnerPermissionAndPending(reservationId, shopId, ownerId);
        reservation.changeStatus(ReservationStatus.CONFIRMED);
    }

    @Transactional
    public void cancelReservation(Long shopId, Long reservationId, Long ownerId) {
        Reservation reservation = validateOwnerPermissionAndPending(reservationId, shopId, ownerId);
        reservation.changeStatus(ReservationStatus.CANCELLED);
    }


    private Reservation validateOwnerPermissionAndPending(Long reservationId, Long shopId,
        Long ownerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(ALREADY_PROCESSED);
        }

        if (!reservation.getShop().getId().equals(shopId)) {
            throw new BusinessException(INVALID_REQUEST_DATA);
        }

        if (!reservation.getShop().getUser().getId().equals(ownerId)) {
            throw new BusinessException(FORBIDDEN_ACCESS);
        }

        return reservation;
    }


    @Transactional(readOnly = true)
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(DATA_NOT_FOUND));
    }
}

