package shop.matjalalzz.shop.mapper;

import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopDetailResponse;
import shop.matjalalzz.shop.dto.ShopOwnerDetailResponse;
import shop.matjalalzz.shop.dto.ShopsItem;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ShopMapper {

    public static Shop createToShop(ShopCreateRequest request, User user) {
        return Shop.builder()
            .shopName(request.shopName())
            .roadAddress(request.roadAddress())
            .detailAddress(request.detailAddress())
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
            .imageCount(request.imageCount())
            .build();
    }


    public static ShopDetailResponse shopDetailResponse (Shop shop, List<String> imageListUrl, boolean canEdit, int reviewCount) {
        return ShopDetailResponse.builder()
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
            .reviewCount(reviewCount)
            .images(imageListUrl)
            .canEdit(canEdit)
            .detailAddress(shop.getDetailAddress())
            .build();
    }





    public static ShopOwnerDetailResponse shopOwnerDetailResponse (Shop shop, List<String> imageListUrl, boolean canEdit, int reviewCount) {
        return ShopOwnerDetailResponse.builder()
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
            .reviewCount(reviewCount)
            .images(imageListUrl)
            .canEdit(canEdit)
            .detailAddress(shop.getDetailAddress())
            .businessCode(shop.getBusinessCode())
            .build();
    }

    public static ShopsItem sliceShopToShopsItem(Shop shop, String thumbnailUrl){
        return ShopsItem.builder()
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .category(shop.getCategory())
            .roadAddress(shop.getRoadAddress())
            .detailAddress(shop.getDetailAddress())
            .rating(shop.getRating())
            .thumbnailUrl(thumbnailUrl)
            .build();
    }



}
