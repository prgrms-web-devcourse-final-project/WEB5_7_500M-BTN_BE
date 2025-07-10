package shop.matjalalzz.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "예약 생성 요청 DTO")
public record CreateReservationRequest(

    @Schema(description = "예약 날짜 (yyyy-MM-dd 형식)", example = "2025-08-10")
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-MM-dd이어야 합니다.")
    String date,

    @Schema(description = "예약 시간 (HH:mm 형식)", example = "18:30")
    @NotBlank
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "시간 형식은 HH:mm이어야 합니다.")
    String time,

    @Schema(description = "예약 인원 수", example = "4", minimum = "1")
    @Min(1)
    int headCount,

    @Schema(description = "예약금 (최소 1000원)", example = "5000", minimum = "1000")
    @Min(1000)
    int reservationFee

) {

}
