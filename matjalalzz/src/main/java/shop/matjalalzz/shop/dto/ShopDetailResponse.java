package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopDetailResponse(
    Long shopId,
    String shopName,
    FoodCategory category,
    String description,
    String roadAddress,
    String detailAddress,
    String tel,
    LocalTime openTime,
    LocalTime closeTime,
    Double rating,
    int reservationFee,
    int reviewCount,
    List<String> images,
    boolean canEdit
) {

}
