package shop.matjalalzz.review.dto.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ReviewResponse(
    Long reviewId,
    String userNickname,
    Double rating,
    String content,
    LocalDateTime createdAt,
    List<String> images) {

}
