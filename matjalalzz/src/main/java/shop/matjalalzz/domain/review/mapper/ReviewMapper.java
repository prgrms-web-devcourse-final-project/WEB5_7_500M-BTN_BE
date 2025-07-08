package shop.matjalalzz.domain.review.mapper;

import shop.matjalalzz.domain.mock.MockReservation;
import shop.matjalalzz.domain.mock.MockShop;
import shop.matjalalzz.domain.mock.MockUser;
import shop.matjalalzz.domain.review.dto.ReviewCreateRequest;
import shop.matjalalzz.domain.review.dto.ReviewResponse;
import shop.matjalalzz.domain.review.entity.Review;

public class ReviewMapper {

    private ReviewMapper() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

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
