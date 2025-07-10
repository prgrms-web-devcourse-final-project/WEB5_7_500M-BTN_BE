package shop.matjalalzz.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReviewCreateRequest(
    @Schema(description = "예약 ID")
    @NotNull(message = "예약ID는 필수입니다.")
    Long reservationId,

    @Schema(description = "음식점 ID")
    @NotNull(message = "음식점ID는 필수입니다.")
    Long shopId,

    @Schema(description = "리뷰 내용")
    @NotBlank(message = "본문은 비어있을 수 없습니다.")
    String content,

    @Schema(description = "별점(0.5~5.0)")
    @NotNull(message = "별점은 필수입니다.")
    @DecimalMin(value = "0.5", message = "별점은 최소 0.5 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "별점은 최대 5.0 이하여야 합니다.")
    @Digits(integer = 1, fraction = 2)
    Double rating,

    @Schema(description = "리뷰 이미지 목록")
    List<ReviewImage> images
) {

    public record ReviewImage(
        @Schema(description = "이미지 URL")
        String imageUrl,

        @Schema(description = "버킷 ID")
        String bucketId
    ) {

    }
}
