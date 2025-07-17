package shop.matjalalzz.shop.dao;

import java.time.LocalDateTime;
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

    Optional<Shop> findByBusinessCodeOrRoadAddressAndDetailAddress(String businessCode,
        String roadAddress, String detailAddress);

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

    @Query("""
        SELECT s FROM Shop s 
        WHERE (s.rating < :cursor OR :cursor IS NULL ) 
        AND (:query IS NULL 
            OR s.shopName LIKE %:query%
            OR s.description LIKE %:query%)
        ORDER BY s.rating DESC
        """)
    Slice<Shop> findCursorListByRating(@Param("cursor") Double cursor,
        @Param("query") String query,
        Pageable pageable);

    @Query("""
        SELECT s FROM Shop s 
        WHERE (s.shopName > :cursor OR :cursor IS NULL ) 
        AND (:query IS NULL 
            OR s.shopName LIKE %:query%
            OR s.description LIKE %:query%)
        ORDER BY s.shopName ASC 
        """)
    Slice<Shop> findCursorListByName(@Param("cursor") String cursor, @Param("query") String query,
        Pageable pageable);

    @Query("""
        SELECT s FROM Shop s 
        WHERE (s.createdAt < :cursor OR :cursor IS NULL ) 
        AND (:query IS NULL 
            OR s.shopName LIKE %:query%
            OR s.description LIKE %:query%)
        ORDER BY s.createdAt DESC
        """)
    Slice<Shop> findCursorListByCreatedAt(@Param("cursor") LocalDateTime cursor,
        @Param("query") String query,
        Pageable pageable);

}