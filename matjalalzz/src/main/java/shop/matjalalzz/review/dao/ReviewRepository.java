package shop.matjalalzz.review.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.entity.Shop;

public interface ReviewRepository extends JpaRepository<Review, Long>, CustomReviewRepository {

//    @Query("SELECT r FROM Review r WHERE r.shop.id = :shopId AND (r.id < :cursor OR :cursor IS NULL ) ORDER BY r.id DESC")
//    Slice<Review> findByShopIdAndCursor(@Param("shopId") Long shopId, @Param("cursor") Long cursor,
//        Pageable pageable);

//    @Query("""
//        SELECT new shop.matjalalzz.review.dto.MyReviewResponse(
//            r.id, s.shopName, r.rating, r.content, r.createdAt, null
//        )
//        FROM Review r
//        JOIN r.shop s
//        WHERE r.writer.id = :userId
//            AND (:cursor = 0 OR r.id < :cursor)
//        ORDER BY r.id DESC
//        """)
//    Slice<MyReviewResponse> findByUserIdAndCursor(@Param("userId") Long userId,
//        @Param("cursor") Long cursor,
//        Pageable pageable);


    @Query("select count(r) from Review r where r.shop.id =:shopId")
    int findReviewCount(@Param("shopId") Long shopId);

    Boolean existsByReservationIdAndWriterId(Long reservationId, Long writerId);

    int countReviewByShop(Shop shop);
}