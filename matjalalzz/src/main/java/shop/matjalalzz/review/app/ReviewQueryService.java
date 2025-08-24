package shop.matjalalzz.review.app;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.dto.MyReviewPageResponse;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.dto.projection.ReviewProjection;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.review.mapper.ReviewMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;


    public ReviewPageResponse findReviewPageByShop(Long shopId, Long cursor, int size) {
        Slice<ReviewProjection> reviews = reviewRepository.findByShopIdAndCursor(shopId, cursor,
            PageRequest.of(0, size));
        Long nextCursor = null;
        if (reviews.hasNext()) {
            nextCursor = reviews.getContent().getLast().getReviewId();
        }
        return ReviewMapper.toReviewPageResponseFromProjection(nextCursor, reviews.getContent(),
            BASE_URL);
    }

    public MyReviewPageResponse findMyReviewPage(Long userId, Long cursor, int size) {
        Slice<MyReviewResponse> comments = reviewRepository.findByUserIdAndCursor(userId, cursor,
            PageRequest.of(0, size));

        Long nextCursor = null;
        if (comments.hasNext()) {
            nextCursor = comments.getContent().getLast().reviewId();
        }

        return ReviewMapper.toMyReviewPageResponse(nextCursor, comments);
    }
    
    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(
            () -> new BusinessException(ErrorCode.DATA_NOT_FOUND));

    }
}
