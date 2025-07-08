package shop.matjalalzz.reservation.dto;

import lombok.Builder;

@Builder
public record CreateReservationRequest(
    String date, // yyyy-MM-DD
    String time,
    int headCount,
    int reservationFee
    ) {}
