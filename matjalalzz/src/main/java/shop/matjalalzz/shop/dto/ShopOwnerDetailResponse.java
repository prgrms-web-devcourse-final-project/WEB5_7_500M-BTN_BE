package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopOwnerDetailResponse(
    long shopId,
    String shopName,
    double latitude,
    double longitude,
    FoodCategory category,
    String description,
    String roadAddress,
    String detailAddress,
    String tel,
    LocalTime openTime,
    LocalTime closeTime,
    double rating,
    int reservationFee,
    int reviewCount,
    List<String> images,
    String businessCode,
    Approve approve
) {

}
