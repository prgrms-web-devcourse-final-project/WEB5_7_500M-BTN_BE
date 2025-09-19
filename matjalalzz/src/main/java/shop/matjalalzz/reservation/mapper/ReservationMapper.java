package shop.matjalalzz.reservation.mapper;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.reservation.dto.MyReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse.ReservationContent;
import shop.matjalalzz.reservation.dto.ReservationSummaryDto;
import shop.matjalalzz.reservation.dto.projection.MyReservationProjection;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationMapper {

    public static Reservation toEntity(CreateReservationRequest request, LocalDateTime reservedAt,
        Shop shop, User user) {
        return Reservation.builder()
            .headCount(request.headCount())
            .reservationFee(request.reservationFee())
            .reservedAt(reservedAt)
            .status(ReservationStatus.PENDING)
            .shop(shop)
            .user(user)
            .build();
    }

    public static Reservation toEntity(Party party, User host) {
        return Reservation.builder()
            .headCount(party.getCurrentCount())
            .reservationFee(party.getTotalReservationFee())
            .reservedAt(party.getMetAt())
            .status(ReservationStatus.PENDING)
            .shop(party.getShop())
            .user(host)
            .party(party)
            .build();
    }

    public static ReservationListResponse toReservationListResponse(
        List<ReservationContent> content,
        Long nextCursor
    ) {
        return ReservationListResponse.builder()
            .content(content)
            .nextCursor(nextCursor)
            .build();
    }

    public static CreateReservationResponse toCreateReservationResponse(Reservation reservation) {
        return CreateReservationResponse.builder()
            .reservationId(reservation.getId())
            .shopName(reservation.getShop().getShopName())
            .dateTime(reservation.getReservedAt())
            .headCount(reservation.getHeadCount())
            .status(reservation.getStatus())
            .build();
    }

    public static List<ReservationContent> toReservationContent(List<Reservation> reservations) {
        return reservations.stream()
            .map(res -> ReservationContent.builder()
                .reservationId(res.getId())
                .shopName(
                    res.getShop().getShopName())                      // ← N+1 가능성 있음, fetch join 필요
                .reservedAt(res.getReservedAt())
                .headCount(res.getHeadCount())
                .phoneNumber(
                    res.getUser().getPhoneNumber())           // ← N+1 가능성 있음, fetch join 필요
                .status(res.getStatus())
                .build())
            .toList();
    }

    public static MyReservationResponse toMyReservationResponse(MyReservationProjection view) {
        return MyReservationResponse.builder()
            .reservationId(view.getReservationId())
            .shopName(view.getShopName())
            .name(view.getName())
            .reservationFee(view.getReservationFee())
            .headCount(view.getHeadCount())
            .reservedAt(view.getReservedAt())
            .status(view.getStatus())
            .build();
    }

    public static MyReservationPageResponse toMyReservationPageResponse(Long nextCursor,
        Slice<MyReservationResponse> reservations) {
        return MyReservationPageResponse.builder()
            .nextCursor(nextCursor)
            .content(reservations.getContent())
            .build();
    }

    public static List<ReservationContent> toReservationProjectionContent(
        List<ReservationSummaryDto> rows) {
        var out = new java.util.ArrayList<ReservationContent>(rows.size());
        for (var r : rows) {
            out.add(ReservationContent.builder()
                .reservationId(r.reservationId())
                .shopName(r.shopName())
                .reservedAt(r.reservedAt())
                .headCount(r.headCount())
                .phoneNumber(r.phoneNumber())
                .status(r.status())
                .build());
        }
        return out;
    }

}
