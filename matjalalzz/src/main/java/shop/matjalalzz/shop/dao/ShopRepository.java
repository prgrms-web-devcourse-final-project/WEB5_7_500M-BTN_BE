package shop.matjalalzz.shop.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.shop.dto.projection.OwnerShopProjection;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

public interface ShopRepository extends JpaRepository<Shop, Long>, ShopRepositoryCustom {

    List<Shop> findByApprove(Approve approve);

    Optional<Shop> findByIdAndApprove(Long id, Approve approved);

    Optional<Shop> findByBusinessCodeOrRoadAddressAndDetailAddress(String businessCode,
        String roadAddress, String detailAddress);

    List<Shop> findByUserId(Long id);

    //거리 순
    @Query("""
            SELECT s FROM Shop s
            WHERE function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) <= :radius
            AND s.category IN :categories
            AND s.approve = :approve
            AND (:cursor IS NULL OR function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) > :cursor)
            ORDER BY function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) ASC
        """)
    Slice<Shop> findByDistanceAndApprove(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radius") double radius,
        @Param("approve") Approve approve,
        @Param("categories") List<FoodCategory> categories,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    //평점 순
    @Query("""
            SELECT s FROM Shop s
            WHERE function('ST_Distance_Sphere', point(s.longitude, s.latitude), point(:lng, :lat)) <= :radius
            AND s.category IN :categories
            AND s.approve = :approve
            AND (:cursor IS NULL OR s.rating < :cursor)
            ORDER BY s.rating DESC
        """)
    Slice<Shop> findByRatingCursorAndApprove(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("radius") double radius,
        @Param("categories") List<FoodCategory> categories,
        @Param("cursor") Double cursor,
        @Param("approve") Approve approve,
        Pageable pageable
    );

    Optional<Shop> findByIdAndUserId(Long id, Long userId);

    User user(User user);



    @Query(value = """
        SELECT
            s.shop_id      AS shopId,
            s.shop_name    AS shopName,
            s.category AS foodCategory,
            s.road_address AS roadAddress,
            s.detail_address AS detailAddress,
            s.rating        AS rating,
            s.approve       AS approve,
            (
              SELECT i.s3key                          
              FROM image i
              WHERE i.shop_id = s.shop_id
              ORDER BY i.image_index ASC
              LIMIT 1
            ) AS firstS3Key
        FROM shop s
        WHERE s.user_id = :userId
        """, nativeQuery = true)
    List<OwnerShopProjection> findOwnerShopsWithFirstImage(@Param("userId") Long userId);

}