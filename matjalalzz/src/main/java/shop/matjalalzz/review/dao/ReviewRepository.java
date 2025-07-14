package shop.matjalalzz.review.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.shop.id = :shopId AND (r.id < :cursor OR :cursor = 0) ORDER BY r.id DESC")
    Slice<Review> findByShopIdAndCursor(@Param("shopId") Long shopId, @Param("cursor") Long cursor,
        Pageable pageable);
}