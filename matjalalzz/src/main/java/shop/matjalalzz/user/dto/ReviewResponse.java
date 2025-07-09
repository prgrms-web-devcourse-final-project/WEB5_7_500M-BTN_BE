package shop.matjalalzz.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "리뷰 정보 요약")
public record ReviewResponse(
    @Schema(description = "리뷰 ID", example = "19") Long reviewId,
    @Schema(description = "가게 이름", example = "엽기떡볶이 잠실점") String shopName,
    @Schema(description = "평점", example = "4.5") double rating,
    @Schema(description = "리뷰 내용", example = "맵찔이도 먹기 좋았어요!") String content,
    @Schema(description = "작성일", example = "2025-07-01T15:32:00") String createdAt,
    @Schema(description = "리뷰 이미지 목록") List<String> images
) {
}