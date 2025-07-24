package shop.matjalalzz.tosspay.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.tosspay.dao.OrderRepository;
import shop.matjalalzz.tosspay.dto.OrderSaveRequest;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmRequest;
import shop.matjalalzz.tosspay.entity.Order;
import shop.matjalalzz.tosspay.entity.OrderStatus;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;

    @Transactional
    public void saveOrder(OrderSaveRequest request, long userId) {
        User user = userService.getUserById(userId);
        orderRepository.save(new Order(request.orderId(), request.amount(), user));
    }

    @Transactional(readOnly = true)
    public Order validateOrder(TossPaymentConfirmRequest request, Long userId) {
        Order order = orderRepository.findByOrderId(request.orderId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_ORDER);
        }

        if (order.getAmount() != request.amount()) {
            throw new BusinessException(ErrorCode.NOT_MATCH_ORDER);
        }

        if (!order.getOrderId().equals(request.orderId())) {
            throw new BusinessException(ErrorCode.NOT_MATCH_ORDER);
        }

        if (order.getStatus() != OrderStatus.READY) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        return order;
    }

}
