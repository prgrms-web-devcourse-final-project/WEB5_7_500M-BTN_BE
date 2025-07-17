package shop.matjalalzz.review.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ReviewPageResponse(
    Long nextCursor,
    List<ReviewResponse> content
) {

}
