package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopResponse(
    Long shopId,
    String name,
    FoodCategory category,
    String description,
    String address,
    String phone,
    LocalTime openTime,
    LocalTime closeTime,
    Double rating,
    int reservationFee,
    int reviewCount,
    List<String> images
) {

}
