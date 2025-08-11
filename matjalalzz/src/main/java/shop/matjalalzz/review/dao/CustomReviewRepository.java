package shop.matjalalzz.review.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.review.entity.Review;

public interface CustomReviewRepository {

    Slice<Review> findByShopIdAndCursor(Long shopId, Long cursor, Pageable pageable);
}
