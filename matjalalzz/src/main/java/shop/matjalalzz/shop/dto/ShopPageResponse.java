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
        Long shopId,
        String name,
        FoodCategory category,
        String address,
        Double rating,
        String thumbnailUrl
    ) {

    }

}
