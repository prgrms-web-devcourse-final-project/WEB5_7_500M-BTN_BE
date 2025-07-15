package shop.matjalalzz.review.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewResponse;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {

    public static ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
            .reviewId(review.getId())
            .userNickname(review.getWriter().getNickname())
            .rating(review.getRating())
            .content(review.getContent())
            .createdAt(review.getCreatedAt())
            .images(review.getImages().stream().map(Image::getS3Key).toList())
            .build();
    }

    public static ReviewPageResponse toReviewPageResponse(Long nextCursor, List<Review> reviews) {
        return ReviewPageResponse.builder()
            .nextCursor(nextCursor)
            .reviews(reviews.stream().map(ReviewMapper::toReviewResponse).toList())
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
