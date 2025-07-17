package shop.matjalalzz.shop.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopLocationSearchParam(
    Double latitude,
    Double longitude,
    Double radius,
    List<FoodCategory> category
) {}
