package shop.matjalalzz.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "내 리뷰 목록 응답")
public record MyReviewsResponse(
    @Schema(description = "리뷰 목록") List<ReviewResponse> content,
    @Schema(description = "다음 커서 ID", example = "20") Long nextCursor
) {
}