package shop.matjalalzz.shop.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;

public record AdminFindShopInfo (

    Shop shop,
    User owner,
    List<String> images

){}



