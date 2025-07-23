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
import shop.matjalalzz.tosspay.mapper.OrderMapper;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void saveOrder(OrderSaveRequest request) {
        if (request.amount() <= 0) {
            throw new BusinessException(ErrorCode.ZERO_AMOUNT_PAYMENT_NOT_ALLOWED);
        }

        orderRepository.save(OrderMapper.toEntity(request));
    }

    @Transactional(readOnly = true)
    public Order validateOrder(TossPaymentConfirmRequest request) {
        Order order = orderRepository.findByOrderId(request.orderId())
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getAmount() != request.amount()) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        return order;
    }

}
