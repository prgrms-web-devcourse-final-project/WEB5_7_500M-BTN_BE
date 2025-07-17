package shop.matjalalzz.shop.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByBusinessCodeOrRoadAddressAndDetailAddress(String businessCode, String roadAddress, String detailAddress);
    
    List<Shop> findByUser(User user);

    List<Shop> findShopsById(Long id);

//
//
//    @Query("""
//SELECT s FROM Shop s
//WHERE
//    function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) <= :radius
//    AND (:categories IS NULL OR s.category IN :categories)
//    AND (
//        :lastDistance IS NULL
//        OR function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) > :lastDistance
//        OR (
//            function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) = :lastDistance
//            AND s.id < :lastId
//        )
//    )
//ORDER BY function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) ASC, s.id DESC
//""")
//    List<Shop> findByDistanceCursorPaging(
//        @Param("lat") Double lat,
//        @Param("lng") Double lng,
//        @Param("radius") Double radius,
//        @Param("categories") List<FoodCategory> categories,
//        @Param("lastDistance") Double lastDistance,
//        @Param("lastId") Long lastId,
//        @Param("limit") int limit
//    );
//
//
//    @Query("""
//SELECT s FROM Shop s
//WHERE
//    function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) <= :radius
//    AND (:categories IS NULL OR s.category IN :categories)
//    AND (
//        :lastRating IS NULL
//        OR s.rating < :lastRating
//        OR (s.rating = :lastRating AND s.id < :lastId)
//    )
//ORDER BY s.rating DESC, s.id DESC
//""")
//    List<Shop> findByRatingCursorPaging(
//        @Param("lat") Double lat,
//        @Param("lng") Double lng,
//        @Param("radius") Double radius,
//        @Param("categories") List<FoodCategory> categories,
//        @Param("lastRating") Double lastRating,
//        @Param("lastId") Long lastId,
//        @Param("limit") int limit
//    );


}