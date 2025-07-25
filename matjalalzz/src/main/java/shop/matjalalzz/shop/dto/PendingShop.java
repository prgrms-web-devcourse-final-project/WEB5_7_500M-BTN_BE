package shop.matjalalzz.shop.dto;

import lombok.Builder;
import shop.matjalalzz.shop.entity.Approve;

@Builder
public record PendingShop (
    String shopName,
    long shopId,
    String userName,
    long userId,
    String address,
    String detailAddress,
    String tel,
    Approve approve
) {}
