package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopsItem (
    Long shopId,
    String shopName,
    FoodCategory category,
    String roadAddress,
    String detailAddress,
    Double rating,
    String thumbnailUrl)
{}
