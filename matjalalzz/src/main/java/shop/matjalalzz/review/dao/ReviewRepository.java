package shop.matjalalzz.review.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.review.dto.projection.MyReviewProjection;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.entity.Shop;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.shop.id = :shopId AND (r.id < :cursor OR :cursor IS NULL ) ORDER BY r.id DESC")
    Slice<Review> findByShopIdAndCursor(@Param("shopId") Long shopId, @Param("cursor") Long cursor,
        Pageable pageable);

    @Query("""
        SELECT r.id AS reviewId, s.shopName AS shopName, r.rating AS rating, r.content AS content,
               r.createdAt AS createdAt
        FROM Review r
        JOIN r.shop s
        WHERE r.writer.id = :userId
            AND (:cursor IS NULL OR r.id < :cursor)
        ORDER BY r.id DESC
        """)
    Slice<MyReviewProjection> findByUserIdAndCursor(@Param("userId") Long userId,
        @Param("cursor") Long cursor,
        Pageable pageable);

    @Query("select count(r) from Review r where r.shop.id =:shopId")
    int findReviewCount(@Param("shopId") Long shopId);

    Boolean existsByReservationIdAndWriterId(Long reservationId, Long writerId);

    int countReviewByShop(Shop shop);
}