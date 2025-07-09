package shop.matjalalzz.reservation.dto;

import lombok.Builder;

@Builder
public record CreateReservationResponse(
    Long reservationId,
    String shopName,
    String dateTime,
    int headCount,
    String status
) {}
