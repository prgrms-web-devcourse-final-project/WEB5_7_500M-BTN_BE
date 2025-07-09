package shop.matjalalzz.domain.review.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.domain.mock.MockReservation;
import shop.matjalalzz.domain.mock.MockShop;
import shop.matjalalzz.domain.mock.MockUser;
import shop.matjalalzz.domain.review.dto.entity.ReviewCreateRequest;
import shop.matjalalzz.domain.review.dto.entity.ReviewResponse;
import shop.matjalalzz.domain.review.entity.Review;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {

    public static ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
            .reviewId(review.getId())
            .userNickname(review.getWriter().getNickname())
            .rating(review.getRating())
            .content(review.getContent())
            .createdAt(review.getCreatedAt())
            .images(null)
            .build();
    }

    public static Review fromReviewCreateRequest(ReviewCreateRequest request, MockUser writer,
        MockShop shop, MockReservation reservation) {
        return Review.builder()
            .writer(writer)
            .shop(shop)
            .reservation(reservation)
            .content(request.content())
            .rating(request.rating())
            .build();
    }
}
