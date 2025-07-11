package shop.matjalalzz.shop.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopResponse;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ShopMapper {

    public static Shop createToShop(ShopCreateRequest request, User user) {
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
            .openTime(request.openTime())
            .closeTime(request.closeTime())
            .owner(user)
            .build();
    }

    public static ShopResponse getToShopResponse (Shop shop) {
        ShopResponse.builder()
            .
    }


}
