package shop.matjalalzz.domain.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.domain.review.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.shop.id = :shopId AND r.id < :cursor ORDER BY r.id DESC")
    Page<Review> findByShopIdAndCursor(@Param("shopId") Long shopId, @Param("cursor") Long cursor, Pageable pageable);
}