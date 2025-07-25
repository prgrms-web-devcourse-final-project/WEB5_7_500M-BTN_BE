package shop.matjalalzz.shop.mapper;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.shop.dto.GetAllPendingShopListResponse;
import shop.matjalalzz.shop.dto.OwnerShopItem;
import shop.matjalalzz.shop.dto.PendingShop;
import shop.matjalalzz.shop.dto.ShopAdminDetailResponse;
import shop.matjalalzz.shop.dto.ShopCreateRequest;
import shop.matjalalzz.shop.dto.ShopDetailResponse;
import shop.matjalalzz.shop.dto.ShopOwnerDetailResponse;
import shop.matjalalzz.shop.vo.ShopUpdateVo;
import shop.matjalalzz.shop.dto.ShopPageResponse;
import shop.matjalalzz.shop.dto.ShopPageResponse.ShopElementResponse;
import shop.matjalalzz.shop.dto.ShopUpdateRequest;
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
            .build();
    }



    public static ShopUpdateVo updateToShop(ShopUpdateRequest request) {
        return ShopUpdateVo.builder()
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
            .build();
    }



    public static ShopDetailResponse shopDetailResponse (Shop shop, List<String> imageListUrl, int reviewCount) {
        return ShopDetailResponse.builder()
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .latitude(shop.getLatitude())
            .longitude(shop.getLongitude())
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
            .detailAddress(shop.getDetailAddress())
            .approve(shop.getApprove())
            .build();
    }





    public static ShopOwnerDetailResponse shopOwnerDetailResponse (Shop shop, List<String> imageListUrl, int reviewCount) {
        return ShopOwnerDetailResponse.builder()
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .category(shop.getCategory())
            .latitude(shop.getLatitude())
            .longitude(shop.getLongitude())
            .description(shop.getDescription())
            .roadAddress(shop.getRoadAddress())
            .tel(shop.getTel())
            .openTime(shop.getOpenTime())
            .closeTime(shop.getCloseTime())
            .rating(shop.getRating())
            .reservationFee(shop.getReservationFee())
            .reviewCount(reviewCount)
            .images(imageListUrl)
            .detailAddress(shop.getDetailAddress())
            .businessCode(shop.getBusinessCode())
            .approve(shop.getApprove())
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
            .latitude(shop.getLatitude())
            .longitude(shop.getLongitude())
            .thumbnailUrl(thumbnailUrl)
            .build();
    }

    public static ShopPageResponse toShopPageResponse(String nextCursor, List<Shop> shops,
        List<String> thumbnails) {
        List<ShopElementResponse> shopList = new ArrayList<>();
        for (int i = 0; i < shops.size(); i++) {
            shopList.add(shopToShopElementResponse(shops.get(i), thumbnails.get(i)));
        }
        return ShopPageResponse.builder()
            .nextCursor(nextCursor)
            .shops(shopList)
            .build();
    }

    public static ShopElementResponse shopToShopElementResponse(Shop shop, String thumbnailUrl) {
        return ShopElementResponse.builder()
            .shopId(shop.getId())
            .name(shop.getShopName())
            .category(shop.getCategory())
            .address(shop.getRoadAddress())
            .detailAddress(shop.getDetailAddress())
            .rating(shop.getRating())
            .thumbnailUrl(thumbnailUrl)
            .latitude(shop.getLatitude())
            .longitude(shop.getLongitude())
            .build();
    }

    public static OwnerShopItem shopToOwnerShopItem(Shop shop, String thumbnailUrl) {
        return OwnerShopItem.builder()
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .category(shop.getCategory())
            .roadAddress(shop.getRoadAddress())
            .detailAddress(shop.getDetailAddress())
            .thumbnailUrl(thumbnailUrl)
            .approve(shop.getApprove())
            .rating(shop.getRating()).build();

    }

    public static ShopAdminDetailResponse shopToShopAdminDetailResponse (Shop shop, List<String> images, User user) {
        return ShopAdminDetailResponse.builder()
            .userId(user.getId())
            .name(user.getName())
            .nickName(user.getName())
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .latitude(shop.getLatitude())
            .longitude(shop.getLongitude())
            .category(shop.getCategory())
            .description(shop.getDescription())
            .roadAddress(shop.getRoadAddress())
            .detailAddress(shop.getDetailAddress())
            .tel(shop.getTel())
            .openTime(shop.getOpenTime())
            .closeTime(shop.getCloseTime())
            .rating(shop.getRating())
            .reservationFee(shop.getReservationFee())
            .images(images)
            .businessCode(shop.getBusinessCode())
            .approve(shop.getApprove())
            .build();


    }

    public static GetAllPendingShopListResponse getAllPendingShopResponse(List<Shop> shopList) {
        List<PendingShop> pendingShopList = shopList.stream().map(ShopMapper::shopToPendingShop).toList();
        return new GetAllPendingShopListResponse(pendingShopList);
    }


    public static PendingShop shopToPendingShop(Shop shop) {
        return PendingShop.builder()
            .shopId(shop.getId())
            .shopName(shop.getShopName())
            .userId(shop.getUser().getId())
            .userName(shop.getUser().getName())
            .tel(shop.getTel())
            .address(shop.getRoadAddress())
            .detailAddress(shop.getDetailAddress())
            .approve(shop.getApprove())
            .build();
    }



}
