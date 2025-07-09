package shop.matjalalzz.review.dto.entity;

import java.util.List;
import lombok.Builder;

@Builder
public record ReviewPageResponse(
    Long nextCursor,
    List<ReviewResponse> reviews
) {


}
