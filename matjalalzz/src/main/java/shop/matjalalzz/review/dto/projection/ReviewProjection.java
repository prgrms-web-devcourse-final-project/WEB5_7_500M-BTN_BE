package shop.matjalalzz.review.dto.projection;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReviewProjection {

    private Long reviewId;
    private Long writerId;
    private Double rating;
    private String content;
    private LocalDateTime createdAt;

}

