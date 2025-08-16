package shop.matjalalzz.review.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.ReviewProjection;

public interface CustomReviewRepository {

    Slice<ReviewProjection> findByShopIdAndCursor(Long shopId, Long cursor, Pageable pageable);

    Slice<MyReviewResponse> findByUserIdAndCursor(Long userId, Long cursor, Pageable pageable);
}
