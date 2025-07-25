package shop.matjalalzz.shop.dto;

import java.util.List;

public record GetAllPendingShopListResponse (
    List<PendingShop> pendingShopList
)
{}
