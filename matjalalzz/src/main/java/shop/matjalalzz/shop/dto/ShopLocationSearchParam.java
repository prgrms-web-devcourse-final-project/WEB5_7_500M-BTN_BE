package shop.matjalalzz.shop.dto;

import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopLocationSearchParam(
    Double latitude,
    Double longitude,
    Double radius,
    FoodCategory category
) {

    public ShopLocationSearchParam {
        radius = radius != null ? radius : 3.0;
    }
}