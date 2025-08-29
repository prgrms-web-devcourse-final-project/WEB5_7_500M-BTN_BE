package shop.matjalalzz.review.dto.projection;

import java.time.LocalDateTime;

public interface MyReviewProjection {
    long getReviewId();
    String getShopName();
    double getRating();
    String getContent();
    LocalDateTime getCreatedAt();
}
