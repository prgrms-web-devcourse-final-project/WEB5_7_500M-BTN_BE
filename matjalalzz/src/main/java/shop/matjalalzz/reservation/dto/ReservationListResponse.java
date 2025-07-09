package shop.matjalalzz.reservation.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ReservationListResponse(
    List<ReservationSummary> content,
    Long nextCursor
) {
    @Builder
    public record ReservationSummary(
        Long reservationId,
        String shopName,
        String reservedAt,
        int headCount,
        String phoneNumber
    ) {}
}
