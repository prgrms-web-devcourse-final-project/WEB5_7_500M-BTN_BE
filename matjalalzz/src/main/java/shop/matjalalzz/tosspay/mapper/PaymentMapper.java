package shop.matjalalzz.tosspay.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmResponse;
import shop.matjalalzz.tosspay.entity.Order;
import shop.matjalalzz.tosspay.entity.Payment;
import shop.matjalalzz.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentMapper {

    public static Payment toEntity(TossPaymentConfirmResponse response, User user, Order order) {
        return Payment.builder()
            .user(user)
            .method(response.method())
            .paymentKey(response.paymentKey())
            .totalAmount(response.totalAmount())
            .status(response.status())
            .order(order)
            .build();
    }

}
