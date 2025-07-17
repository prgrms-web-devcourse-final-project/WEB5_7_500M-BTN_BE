package shop.matjalalzz.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import shop.matjalalzz.reservation.entity.ReservationStatus;

@Builder
@Schema(description = "예약 목록 응답 DTO")
public record ReservationListResponse(

    @Schema(description = "예약 요약 정보 리스트")
    List<ReservationContent> content,

    @Schema(description = "다음 페이지 커서. 더 이상 없으면 null", example = "103")
    Long nextCursor

) {

    @Builder
    @Schema(description = "예약 요약 정보 DTO")
    public record ReservationContent(

        @Schema(description = "예약 ID", example = "101")
        Long reservationId,

        @Schema(description = "식당 이름", example = "맛잘알 고기집")
        String shopName,

        @Schema(description = "예약 일시 (yyyy-MM-dd HH:mm)", example = "2025-08-10 18:30")
        LocalDateTime reservedAt,

        @Schema(description = "예약 인원 수", example = "3")
        int headCount,

        @Schema(description = "예약자 전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "예약자 전화번호", example = "010-1234-5678")
        ReservationStatus status

    ) {

    }
}
