package shop.matjalalzz.shop.dto;

import java.util.List;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopLocationSearchParam(
    Double latitude,
    Double longitude,
    Double radius,
    List<FoodCategory> category
) {

    public ShopLocationSearchParam {
        latitude = latitude != null ? latitude :  37.5724;      //종로구 좌표값
        longitude = longitude != null ? longitude : 126.9794;
        radius = radius != null ? radius : 3.0;
        category = (category != null && !category.isEmpty()) ? category : List.of(FoodCategory.values()); // 전체 카테고리가 기본값
    }
}