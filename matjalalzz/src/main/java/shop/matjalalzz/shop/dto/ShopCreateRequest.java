package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record ShopCreateRequest(
    String shopName,
    String roadAddress,
    String detailAddress,
    String sido,
    Double latitude,
    Double longitude,
    String businessCode,
    String category,
    int reservationFee,
    LocalTime openTime,
    LocalTime closeTime,
    String description
) {

}
