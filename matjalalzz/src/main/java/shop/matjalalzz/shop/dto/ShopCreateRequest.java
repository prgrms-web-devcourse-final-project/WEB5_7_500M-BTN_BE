package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopCreateRequest(
    String shopName,
    String roadAddress,
    String detailAddress,
    String sido,
    String tel,
    Double latitude,
    Double longitude,
    String businessCode,
    FoodCategory category,
    int reservationFee,
    LocalTime openTime,
    LocalTime closeTime,
    String description
) {

}
