package shop.matjalalzz.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "예약 생성 응답 DTO")
public record CreateReservationResponse(

    @Schema(description = "예약 ID", example = "101")
    Long reservationId,

    @Schema(description = "식당 이름", example = "맛잘알 고기집")
    String shopName,

    @Schema(description = "예약 일시 (yyyy-MM-dd HH:mm)", example = "2025-08-10 18:30")
    String dateTime,

    @Schema(description = "예약 인원 수", example = "4")
    int headCount,

    @Schema(description = "예약 상태", example = "PENDING")
    String status

) {

}
