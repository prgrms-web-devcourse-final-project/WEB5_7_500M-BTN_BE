package shop.matjalalzz.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ReviewCreateRequest(
    @Schema(description = "예약 ID", required = true)
    @NotBlank(message = "예약ID는 필수입니다.")
    Long reservationId,

    @Schema(description = "리뷰 내용", required = true)
    @NotBlank(message = "본문은 비어있을 수 없습니다.")
    String content,

    @Schema(description = "별점(1.0~5.0)", required = true)
    @NotBlank(message = "별점은 필수입니다.")
    Double rating,

    @Schema(description = "리뷰 이미지 목록", required = false)
    List<ReviewImage> images
) {

    public record ReviewImage(
        @Schema(description = "이미지 URL", required = true)
        String imageUrl,

        @Schema(description = "버킷 ID", required = true)
        String bucketId
    ) {

    }
}
