package shop.matjalalzz.review.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.review.dto.projection.ReviewProjection;

public interface CustomReviewRepository {

    Slice<ReviewProjection> findByShopIdAndCursor(Long shopId, Long cursor, Pageable pageable);
}
