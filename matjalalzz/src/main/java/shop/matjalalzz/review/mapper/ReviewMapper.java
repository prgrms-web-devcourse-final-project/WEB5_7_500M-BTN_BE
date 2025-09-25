package shop.matjalalzz.review.mapper;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.review.dto.MyReviewPageResponse;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.dto.ReviewResponse;
import shop.matjalalzz.review.dto.projection.MyReviewProjection;
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
        String nickname, List<String> images, String baseURL) {
        return ReviewResponse.builder()
            .reviewId(review.getReviewId())
            .userNickname(nickname)
            .rating(review.getRating())
            .content(review.getContent())
            .createdAt(review.getCreatedAt())
            .images(images.stream().map(i -> baseURL + i).toList())
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
        List<ReviewProjection> reviews, Map<Long, String> nicknames, Map<Long, List<String>> images,
        String baseURL) {
        return ReviewPageResponse.builder()
            .nextCursor(nextCursor)
            .content(reviews.stream().map(r -> {
                String nickname = nicknames.get(r.getReviewId());
                List<String> imageList = images.get(r.getReviewId());
                return toReviewResponseFromProjection(r, nickname, imageList, baseURL);
            }).toList())
            .build();
    }

    public static MyReviewResponse toMyReviewResponse(MyReviewProjection view, List<String> reviewImages) {
        return MyReviewResponse.builder()
            .reviewId(view.getReviewId())
            .shopName(view.getShopName())
            .rating(view.getRating())
            .content(view.getContent())
            .createdAt(view.getCreatedAt())
            .images(reviewImages)
            .build();
    }

    public static MyReviewPageResponse toMyReviewPageResponse(Long nextCursor,
        Slice<MyReviewProjection> reviews, Map<Long, List<String>> reviewImages) {
        List<MyReviewResponse> responses = reviews.stream()
            .map(v -> toMyReviewResponse(v, reviewImages.get(v.getReviewId())))
            .toList();

        return MyReviewPageResponse.builder()
            .nextCursor(nextCursor)
            .content(responses)
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
