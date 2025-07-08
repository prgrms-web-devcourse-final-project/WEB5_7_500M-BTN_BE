package shop.matjalalzz.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CreateReservationRequest(

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-MM-dd이어야 합니다.")
    String date,

    @NotBlank
    @Pattern(regexp = "\\d{2}:\\d{2}", message = "시간 형식은 HH:mm이어야 합니다.")
    String time,

    @Min(1)
    int headCount,

    @Min(1000)
    int reservationFee
    ) {}
