package shop.matjalalzz.shop.dto;

import jakarta.persistence.Column;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopsItem (
    long shopId,
    String shopName,
    FoodCategory category,
    String roadAddress,
    String detailAddress,
    double latitude, //위도
    double longitude, //경도
    Double rating,
    String thumbnailUrl,
    Double distance
    )
{}
