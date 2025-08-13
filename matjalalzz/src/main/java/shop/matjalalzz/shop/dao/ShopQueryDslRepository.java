package shop.matjalalzz.shop.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.image.entity.QImage;
import shop.matjalalzz.shop.dto.AdminFindShopInfo;
import shop.matjalalzz.shop.dto.ShopsItem;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.QShop;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.QUser;

@Repository
@RequiredArgsConstructor
public class ShopQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    QShop shop = QShop.shop;
    QUser user = QUser.user;
    QImage image = QImage.image;


    public List<ShopsItem> findAllShops(double latitude, double longitude, double radius
        , List<FoodCategory> foodCategories, Double distanceOrRating, int size, String sort,
        Long shopId) {

        // 거리 계산 공식
        NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
            "ST_Distance_Sphere(POINT({0}, {1}), POINT({2}, {3}))",
            longitude, latitude, shop.longitude, shop.latitude
        );

        // BooleanBuilder는 조건을 유동적으로 붙일 수 있는 객체
        BooleanBuilder baseCondition = new BooleanBuilder();
        baseCondition.and(shop.approve.eq(Approve.APPROVED)); // 식당 등록 상태가 APPROVED인 상점만 대상으로 함

        double latDeg = radius / 111_320d; // 1도 ≈ 111.32km
        double lonDeg = radius / (111_320d * Math.cos(Math.toRadians(latitude)));
        baseCondition.and(shop.latitude.between(latitude - latDeg, latitude + latDeg));
        baseCondition.and(shop.longitude.between(longitude - lonDeg, longitude + lonDeg));

        baseCondition.and(distance.loe(radius)); // 반경 필터
        baseCondition.and(shop.category.in(foodCategories)); // 카테고리들만 가져오기 (선택하지 않으몬 모든 카테고리)

        //커서 조건 설정하는 부분
        if (distanceOrRating != null && shopId != null) {
            if ("distance".equals(sort)) {
                baseCondition.and(distance.gt(
                            distanceOrRating) //distance 값이 cursor보다 크다 (greater than로 이전 마지막 거리보다 먼 애들을 가져오기 위해)
                        .or(distance.eq(distanceOrRating).and(shop.id.goe(shopId)))
                    // 다음 거리거나, 같은 거리 안에서 뒤에 있는 상점만 가져오기
                );
            } else if ("rating".equals(sort)) {
                baseCondition.and(shop.rating.lt(
                        distanceOrRating) //rating 값이 cursor보다 작다 (작은 값을 기준으로 점점 내려가는 형태로)
                    .or(shop.rating.eq(distanceOrRating).and(shop.id.gt(shopId)))
                );
            }
        }

        //정렬 조건 설정하는 부분
        List<OrderSpecifier<?>> orderSpecifiers;
        if ("rating".equals(sort)) {
            orderSpecifiers = List.of(
                shop.rating.desc().nullsLast(),
                shop.id.asc()
            );
        } else {
            orderSpecifiers = List.of(
                distance.asc(),
                shop.id.asc()
            );
        }

        //쿼리 실행
        return queryFactory
            .select(Projections.constructor(ShopsItem.class,
                shop.id,
                shop.shopName,
                shop.category,
                shop.roadAddress,
                shop.detailAddress,
                shop.latitude,
                shop.longitude,
                shop.rating,
                Expressions.constant(""), // thumbnailUrl이 이 시점에는 없으니 빈 문자열로
                distance
            ))
            .from(shop)
            .where(baseCondition)
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .limit(size + 1) // 다음 페이지 확인을 위한 size+1
            .fetch(); // 쿼리를 실행하고 결과를 즉시 리스트(List)로 반환 (List<ShopsItem>로)
    }


    public AdminFindShopInfo adminFindShop(long shopId) {

        Tuple result = queryFactory
            .select(shop, user)
            .from(shop)
            .where(shop.id.eq(shopId))
            .join(shop.user, user).fetchJoin()
            .fetchOne();

        if (result == null) {
            return null;
        }

        List<String> images = queryFactory
            .select(image.s3Key)
            .from(image)
            .where(image.shopId.eq(shopId))
            .fetch();

        return new AdminFindShopInfo(
            result.get(shop),
            result.get(user),
            images
        );


    }

    public Slice<Shop> findCursorListByRating(Double cursor, String query, Approve approve,
        Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();

        // 커서 조건
        if (cursor != null) {
            condition.and(shop.rating.lt(cursor));
        }

        // 승인 상태 조건
        condition.and(shop.approve.eq(approve));

        // 검색 조건
        if (query != null && !query.trim().isEmpty()) {
            condition.and(
                shop.shopName.containsIgnoreCase(query)
                    .or(shop.description.containsIgnoreCase(query))
            );
        }

        List<Shop> shops = queryFactory
            .selectFrom(shop)
            .where(condition)
            .orderBy(shop.rating.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = shops.size() > pageable.getPageSize();
        if (hasNext) {
            shops.remove(shops.size() - 1);
        }

        return new SliceImpl<>(shops, pageable, hasNext);
    }

    public Slice<Shop> findCursorListByName(String cursor, String query, Approve approve,
        Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();

        // 커서 조건
        if (cursor != null) {
            condition.and(shop.shopName.gt(cursor));
        }

        // 승인 상태 조건
        condition.and(shop.approve.eq(approve));

        // 검색 조건
        if (query != null && !query.trim().isEmpty()) {
            condition.and(
                shop.shopName.containsIgnoreCase(query)
                    .or(shop.description.containsIgnoreCase(query))
            );
        }

        List<Shop> shops = queryFactory
            .selectFrom(shop)
            .where(condition)
            .orderBy(shop.shopName.asc())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = shops.size() > pageable.getPageSize();
        if (hasNext) {
            shops.remove(shops.size() - 1);
        }

        return new SliceImpl<>(shops, pageable, hasNext);
    }

    public Slice<Shop> findCursorListByCreatedAt(LocalDateTime cursor, String query,
        Approve approve, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();

        // 커서 조건
        if (cursor != null) {
            condition.and(shop.createdAt.lt(cursor));
        }

        // 승인 상태 조건
        condition.and(shop.approve.eq(approve));

        // 검색 조건
        if (query != null && !query.trim().isEmpty()) {
            condition.and(
                shop.shopName.containsIgnoreCase(query)
                    .or(shop.description.containsIgnoreCase(query))
            );
        }

        List<Shop> shops = queryFactory
            .selectFrom(shop)
            .where(condition)
            .orderBy(shop.createdAt.desc())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = shops.size() > pageable.getPageSize();
        if (hasNext) {
            shops.remove(shops.size() - 1);
        }

        return new SliceImpl<>(shops, pageable, hasNext);
    }
}
