package shop.matjalalzz.shop.dto;

import lombok.Builder;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record OwnerShopItem (
        long shopId,
        String shopName,
        FoodCategory category,
        String roadAddress,
        String detailAddress,
        double rating,
        Approve approve,
        String thumbnailUrl
){}
