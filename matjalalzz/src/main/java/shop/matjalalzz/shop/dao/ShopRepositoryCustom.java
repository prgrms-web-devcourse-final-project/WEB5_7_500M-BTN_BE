package shop.matjalalzz.shop.dao;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.shop.dto.AdminFindShopInfo;
import shop.matjalalzz.shop.dto.ShopsItem;
import shop.matjalalzz.shop.entity.Approve;
import shop.matjalalzz.shop.entity.FoodCategory;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.entity.ShopListSort;

public interface ShopRepositoryCustom {

   List<ShopsItem> findDistanceOrRatingShopsQdsl(double latitude, double longitude, double radius
        , List<FoodCategory> foodCategories, Double distanceOrRating, int size, String sort, Long shopId);

    AdminFindShopInfo adminFindShop(long shopId);

    Slice<Shop> findShopCursorList(Object cursor, String query, Approve approve,
        Pageable pageable, ShopListSort sort);


}
