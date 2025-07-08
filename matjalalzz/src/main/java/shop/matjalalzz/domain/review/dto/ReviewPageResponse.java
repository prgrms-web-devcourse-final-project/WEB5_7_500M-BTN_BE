package shop.matjalalzz.domain.review.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ReviewPageResponse(
    Long nextCursor,
    List<ReviewResponse> reviews
) {


}
