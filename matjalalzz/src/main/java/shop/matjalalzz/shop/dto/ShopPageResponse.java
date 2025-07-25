package shop.matjalalzz.shop.dto;

import java.util.List;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopPageResponse(
    String nextCursor,
    List<ShopElementResponse> shops
) {

    @Builder
    public record ShopElementResponse(
        long shopId,
        String name,
        FoodCategory category,
        String address,
        String detailAddress,
        double rating,
        String thumbnailUrl,
        double latitude, //위도
        double longitude //경도

    ) {

    }

}
