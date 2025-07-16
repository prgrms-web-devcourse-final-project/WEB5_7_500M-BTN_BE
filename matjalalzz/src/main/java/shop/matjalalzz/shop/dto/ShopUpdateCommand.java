package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;


@Builder
public record ShopUpdateCommand (
     String shopName,
     String roadAddress,
     String detailAddress,
     String sido,
     Double latitude,
     Double longitude,
     String description,
     FoodCategory category,
     String tel,
     String businessCode,
     int reservationFee,
     LocalTime openTime,
     LocalTime closeTime
)
{}
