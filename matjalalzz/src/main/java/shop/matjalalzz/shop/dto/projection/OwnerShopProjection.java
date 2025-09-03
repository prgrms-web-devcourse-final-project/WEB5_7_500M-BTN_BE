package shop.matjalalzz.shop.dto.projection;

import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;

public interface OwnerShopProjection {
    Long getShopId();
    String getShopName();
    FoodCategory getFoodCategory();
    String getRoadAddress();
    String getDetailAddress();
    double getRating();
    Approve getApprove();
    String getFirstS3Key(); // 대표 이미지(없으면 null)
}
