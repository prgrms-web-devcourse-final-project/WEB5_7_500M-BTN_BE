package shop.matjalalzz.review.mapper;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.review.dto.MyReviewPageResponse;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.dto.ReviewResponse;
import shop.matjalalzz.review.dto.projection.ReviewProjection;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {

    public static ReviewResponse toReviewResponse(Review review, String baseURL) {
        return ReviewResponse.builder()
            .reviewId(review.getId())
            .userNickname(review.getWriter().getNickname())
            .rating(review.getRating())
            .content(review.getContent())
            .createdAt(review.getCreatedAt())
            .images(review.getImages().stream().map(i -> baseURL + i.getS3Key()).toList())
            .build();
    }

    public static ReviewResponse toReviewResponseFromProjection(ReviewProjection review,
        String baseURL) {
        return ReviewResponse.builder()
            .reviewId(review.getReviewId())
            .userNickname(review.getUserNickname())
            .rating(review.getRating())
            .content(review.getContent())
            .createdAt(review.getCreatedAt())
            .images(review.getImages().stream().map(i -> baseURL + i).toList())
            .build();
    }

    public static ReviewPageResponse toReviewPageResponse(Long nextCursor, List<Review> reviews,
        String baseURL) {
        return ReviewPageResponse.builder()
            .nextCursor(nextCursor)
            .content(reviews.stream().map(r -> toReviewResponse(r, baseURL)).toList())
            .build();
    }

    public static ReviewPageResponse toReviewPageResponseFromProjection(Long nextCursor,
        List<ReviewProjection> reviews,
        String baseURL) {
        return ReviewPageResponse.builder()
            .nextCursor(nextCursor)
            .content(reviews.stream().map(r -> toReviewResponseFromProjection(r, baseURL)).toList())
            .build();
    }

    public static MyReviewPageResponse toMyReviewPageResponse(Long nextCursor,
        Slice<MyReviewResponse> reviews) {

        return MyReviewPageResponse.builder()
            .nextCursor(nextCursor)
            .content(reviews.getContent())
            .build();
    }

    public static Review fromReviewCreateRequest(ReviewCreateRequest request, User writer,
        Shop shop, Reservation reservation) {
        return Review.builder()
            .writer(writer)
            .shop(shop)
            .reservation(reservation)
            .content(request.content())
            .rating(request.rating())
            .build();
    }
}
