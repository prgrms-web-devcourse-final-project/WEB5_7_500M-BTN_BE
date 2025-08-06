package shop.matjalalzz.shop.dao;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import shop.matjalalzz.shop.dto.ShopsItem;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.QShop;

@Repository
@RequiredArgsConstructor
public class ShopQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    QShop shop = QShop.shop;


    public List<ShopsItem> findAllShops(double latitude, double  longitude, double radius
        ,List<FoodCategory> foodCategories, Double cursor, int size, String sort) {

        // 거리 계산 공식
        NumberExpression<Double> distance = Expressions.numberTemplate(Double.class,
            "6371000 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
            latitude, shop.latitude, shop.longitude, longitude);


        // BooleanBuilder는 조건을 유동적으로 붙일 수 있는 객체
        BooleanBuilder baseCondition = new BooleanBuilder();
        baseCondition.and(shop.approve.eq(Approve.APPROVED)); // 식당 등록 상태가 APPROVED인 상점만 대상으로 함
        baseCondition.and(distance.loe(radius)); // 반경 필터
        baseCondition.and(shop.category.in(foodCategories)); // 카테고리들만 가져오기 (선택하지 않으몬 모든 카테고리)

        //커서 조건 설정하는 부분
        if (cursor != null) {
            if ("distance".equals(sort)) {
                baseCondition.and(distance.gt(cursor)); //distance 값이 cursor보다 크다 (greater than로 이전 마지막 거리보다 먼 애들을 가져오기 위해)
            } else if ("rating".equals(sort)) {
                baseCondition.and(shop.rating.lt(cursor)); //rating 값이 cursor보다 작다 (작은 값을 기준으로 점점 내려가는 형태로)
            }
        }


        //정렬 조건 설정하는 부분
        OrderSpecifier<?> orderSpecifier = "rating".equals(sort) ? shop.rating.desc(): distance.asc();

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
            .orderBy(orderSpecifier)
            .limit(size+1) // 다음 페이지 확인을 위한 size+1
            .fetch(); // 쿼리를 실행하고 결과를 즉시 리스트(List)로 반환 (List<ShopsItem>로)
    }

}
