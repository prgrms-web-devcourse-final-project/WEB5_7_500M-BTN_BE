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
) {}
