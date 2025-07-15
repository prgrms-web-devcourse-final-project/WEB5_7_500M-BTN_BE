package shop.matjalalzz.shop.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByBusinessCodeOrRoadAddressAndDetailAddress(String businessCode, String roadAddress, String detailAddress);
    
    List<Shop> findByUser(User user);


    //거리 순
    @Query("""
    SELECT s FROM Shop s
    WHERE function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) <= :radius
    AND s.category IN :categories
    AND (:cursor IS NULL OR function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) > :cursor)
    ORDER BY function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) ASC
""")
    Slice<Shop> findByDistance(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radius") double radius,
        @Param("categories") List<FoodCategory> categories,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    //평점 순
    @Query("""
    SELECT s FROM Shop s
    WHERE function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) <= :radius
    AND s.category IN :categories
    AND (:cursor IS NULL OR s.rating < :cursor)
    ORDER BY s.rating DESC
""")
    Slice<Shop> findByRatingCursor(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radius") double radius,
        @Param("categories") List<FoodCategory> categories,
        @Param("cursor") Double cursor,
        Pageable pageable
    );



}