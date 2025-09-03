package shop.matjalalzz.review.app;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.projection.ReviewProjection;
import shop.matjalalzz.review.entity.Review;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    public Slice<ReviewProjection> findReviewPageByShop(Long shopId, Long cursor, int size) {
        Slice<ReviewProjection> reviews = reviewRepository.findByShopIdAndCursor(shopId, cursor,
            PageRequest.of(0, size));

        return reviews;
    }

    public Slice<MyReviewResponse> findReviewPageByUser(Long userId, Long cursor, int size) {
        Slice<MyReviewResponse> reviews = reviewRepository.findByUserIdAndCursor(userId, cursor,
            PageRequest.of(0, size));
        return reviews;
    }

    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(
            () -> new BusinessException(ErrorCode.DATA_NOT_FOUND));

    }

    public void validateDuplicatedReview(Long reservationId, Long writerId) {
        if (reviewRepository.existsByReservationIdAndWriterId(reservationId, writerId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_DATA);
        }
    }

    public int findReviewCountByShop(long shopId) {
        return reviewRepository.findReviewCount(shopId);
    }

}
