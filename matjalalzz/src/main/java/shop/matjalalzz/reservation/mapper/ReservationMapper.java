package shop.matjalalzz.reservation.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.party.entity.Party;

@Component
public class ReservationMapper {

    public Reservation toEntity(CreateReservationRequest request, LocalDateTime reservedAt,
        Shop shop, User user, Party party) {
        return Reservation.builder()
            .headCount(request.headCount())
            .reservationFee(request.reservationFee())
            .reservedAt(reservedAt)
            .status(ReservationStatus.PENDING)
            .shop(shop)
            .user(user)
            .party(party)
            .build();
    }

    public ReservationListResponse toReservationListResponse(
        List<ReservationListResponse.ReservationSummary> content,
        Long nextCursor
    ) {
        return ReservationListResponse.builder()
            .content(content)
            .nextCursor(nextCursor)
            .build();
    }

    public CreateReservationResponse toCreateReservationResponse(Reservation reservation) {
        return CreateReservationResponse.builder()
            .reservationId(reservation.getId())
            .shopName(reservation.getShop().getName())
            .dateTime(reservation.getReservedAt().toString())
            .headCount(reservation.getHeadCount())
            .status(reservation.getStatus().name())
            .build();
    }

    public List<ReservationListResponse.ReservationSummary> toReservationSummaries(List<Reservation> reservations) {
        return reservations.stream()
            .map(res -> ReservationListResponse.ReservationSummary.builder()
                .reservationId(res.getId())
                .shopName(res.getShop().getName())                      // ← N+1 가능성 있음, fetch join 필요
                .reservedAt(res.getReservedAt().toString())
                .headCount(res.getHeadCount())
                .phoneNumber(res.getUser().getPhoneNumber())           // ← N+1 가능성 있음, fetch join 필요
                .build())
            .collect(Collectors.toList());
    }
}
