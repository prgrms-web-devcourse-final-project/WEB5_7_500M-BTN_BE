package shop.matjalalzz.review.dto;

import java.time.LocalDateTime;

public interface MyReviewView {
    long getReviewId();
    String getShopName();
    double getRating();
    String getContent();
    LocalDateTime getCreatedAt();
}
