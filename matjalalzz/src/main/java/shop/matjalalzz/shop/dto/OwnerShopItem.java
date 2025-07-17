package shop.matjalalzz.shop.dto;

import lombok.Builder;

@Builder
public record OwnerShopItem (
        long shopId,
        String shopName,
        double rating,
        int reviewCount
){}
