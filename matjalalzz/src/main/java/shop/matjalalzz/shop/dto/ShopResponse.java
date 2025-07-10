package shop.matjalalzz.shop.dto;

import java.time.LocalTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ShopResponse(
    Long shopId,
    String name,
    String category,
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
