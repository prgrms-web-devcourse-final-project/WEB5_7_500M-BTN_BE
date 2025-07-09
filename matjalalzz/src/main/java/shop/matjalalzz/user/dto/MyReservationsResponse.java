package shop.matjalalzz.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "내 예약 목록 응답")
public record MyReservationsResponse(
    @Schema(description = "예약 목록") List<ReservationResponse> content,
    @Schema(description = "다음 커서 ID", example = "100") Long nextCursor
) {
}