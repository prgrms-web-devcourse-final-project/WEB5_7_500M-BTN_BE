package shop.matjalalzz.tosspay.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
            .orElseThrow(() -> {
                log.error("주문 정보를 찾을 수 없습니다. orderId = {}", request.orderId());
                return new BusinessException(ErrorCode.ORDER_NOT_FOUND);
            });

        if (!order.getUser().getId().equals(userId)) {
            log.error("사용자 정보가 일치하지 않습니다. 요청자 id = {}, 주문자 id = {}", userId,
                order.getUser().getId());
            throw new BusinessException(ErrorCode.NOT_MATCH_ORDER);
        }

        if (order.getAmount() != request.amount()) {
            log.error("결제 금액이 일치하지 않습니다. 기존 결제 금액 = {}, 요청 결제 금액 = {}", order.getAmount(),
                request.amount());
            throw new BusinessException(ErrorCode.NOT_MATCH_ORDER);
        }

        if (!order.getOrderId().equals(request.orderId())) {
            log.error("주문 번호가 일치하지 않습니다. 기존 주문 번호 = {}, 요청 주문 번호 = {}", order.getOrderId(),
                request.orderId());
            throw new BusinessException(ErrorCode.NOT_MATCH_ORDER);
        }

        if (order.getStatus() != OrderStatus.READY) {
            log.error("이미 주문이 처리된 건입니다. orderId = {}", order.getOrderId());
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        return order;
    }

}
