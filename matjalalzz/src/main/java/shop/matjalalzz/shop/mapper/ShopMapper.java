package shop.matjalalzz.shop.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.entity.Shop;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ShopMapper {

    public static Shop createToShop(ShopCreateRequest request, Long userId) {
        return Shop.builder()
            .shopName(request.shopName())
            .roadAddress(request.roadAddress())
            .sido(request.sido())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .description(request.description())
            .category(request.category())
            .tel(request.tel())
            .businessCode(request.businessCode())
            .reservationFee(request.reservationFee())
            .openTime(request.openTime())
            .closeTime(request.closeTime())
            .owner(userId)
            .build();
    }


}
