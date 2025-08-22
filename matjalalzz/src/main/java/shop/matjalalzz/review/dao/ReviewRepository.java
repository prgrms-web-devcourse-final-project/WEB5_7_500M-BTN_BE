package shop.matjalalzz.review.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.entity.Shop;

public interface ReviewRepository extends JpaRepository<Review, Long>, CustomReviewRepository {

    @Query("select count(r) from Review r where r.shop.id =:shopId")
    int findReviewCount(@Param("shopId") Long shopId);

    Boolean existsByReservationIdAndWriterId(Long reservationId, Long writerId);

    int countReviewByShop(Shop shop);
}