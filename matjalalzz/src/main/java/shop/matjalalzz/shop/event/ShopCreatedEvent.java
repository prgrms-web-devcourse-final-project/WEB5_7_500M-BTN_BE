package shop.matjalalzz.shop.event;

import lombok.Getter;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

@Getter
public class ShopCreatedEvent {
    private final User user;
    private final String shopName;
    private final String roadAddress;
    private final String detailAddress;

    public ShopCreatedEvent(Shop shop) {
        this.user = shop.getUser();
        this.shopName = shop.getShopName();
        this.roadAddress = shop.getRoadAddress();
        this.detailAddress = shop.getDetailAddress();
    }
}
