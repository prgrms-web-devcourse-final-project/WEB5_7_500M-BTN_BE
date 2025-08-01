package shop.matjalalzz.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import shop.matjalalzz.reservation.entity.ReservationStatus;

@Builder
@Schema(description = "예약 정보 요약")
public record MyReservationResponse(
    @Schema(description = "예약 ID", example = "90")
    long reservationId,

    @Schema(description = "가게 이름", example = "신전떡볶이 강남점")
    String shopName,

    @Schema(description = "예약자 이름", example = "이초롱")
    String name,

    @Schema(description = "예약 일시", example = "2025-07-10T18:00:00")
    LocalDateTime reservedAt,

    @Schema(description = "예약 인원", example = "2")
    int headCount,

    @Schema(description = "예약금", example = "4000")
    int reservationFee,

    @Schema(description = "예약 상태", example = "CONFIRMED")
    ReservationStatus status
) {

}