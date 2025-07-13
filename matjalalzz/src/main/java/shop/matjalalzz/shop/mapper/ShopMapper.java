package shop.matjalalzz.shop.mapper;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.image.entity.Image;
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
            .user(user)
            .imageCount(request.shopImageCount())
            .build();
    }

    public static Image shopCreateToImage(String key,long index, long shopId) {
        return Image.builder()
            .s3Key(key)
            .imageIndex(index)
            .shopId(shopId)
            .build();
    }

    public static ShopResponse shopDetailResponse (Shop shop, List<String> imageListUrl) {
        return ShopResponse.builder()
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .category(shop.getCategory())
            .description(shop.getDescription())
            .roadAddress(shop.getRoadAddress())
            .tel(shop.getTel())
            .openTime(shop.getOpenTime())
            .closeTime(shop.getCloseTime())
            .rating(shop.getRating())
            .reservationFee(shop.getReservationFee())
            .reviewCount(5)//임시임
            .images(imageListUrl)
            .build();
    }


}
