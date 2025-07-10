package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopCreateRequest(
    //이미지 없는 상태임
    String shopName,
    String roadAddress,
    String detailAddress,
    String sido,
    Double latitude,
    Double longitude,
    String businessCode,
    FoodCategory category,
    String tel,
    int reservationFee,
    LocalTime openTime,
    LocalTime closeTime,
    String description
) {

}
