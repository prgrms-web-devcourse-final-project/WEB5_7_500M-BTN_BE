package shop.matjalalzz.tosspay.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.tosspay.dto.OrderSaveRequest;
import shop.matjalalzz.tosspay.entity.Order;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderMapper {

    public static Order toEntity(OrderSaveRequest request) {
        return Order.builder()
            .orderId(request.orderId())
            .amount(request.amount())
            .build();
    }
}
