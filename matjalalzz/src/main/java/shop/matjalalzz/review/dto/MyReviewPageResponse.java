package shop.matjalalzz.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "내 리뷰 목록 응답")
public record MyReviewPageResponse(

    @Schema(description = "리뷰 목록")
    List<MyReviewResponse> content,

    @Schema(description = "다음 커서 ID", example = "20")
    Long nextCursor
) {

}