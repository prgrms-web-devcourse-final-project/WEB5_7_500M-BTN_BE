package shop.matjalalzz.tosspay.app;


import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.tosspay.config.TossApiClient;
import shop.matjalalzz.tosspay.dao.PaymentRepository;
import shop.matjalalzz.tosspay.dto.PaymentHistoryResponse;
import shop.matjalalzz.tosspay.dto.PaymentScrollResponse;
import shop.matjalalzz.tosspay.dto.PaymentSuccessResponse;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmRequest;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmResponse;
import shop.matjalalzz.tosspay.entity.Order;
import shop.matjalalzz.tosspay.entity.OrderStatus;
import shop.matjalalzz.tosspay.entity.Payment;
import shop.matjalalzz.tosspay.entity.PaymentStatus;
import shop.matjalalzz.tosspay.mapper.PaymentMapper;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossApiClient tossApiClient;
    private final UserService userService;
    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Transactional
    public PaymentSuccessResponse confirmPayment(TossPaymentConfirmRequest tossRequest,
        Long userId) {

        // 1. 유저 검증
        User user = userService.getUserById(userId);

        // 2. 결제 데이터 무결성 확인
        Order order = orderService.validateOrder(tossRequest, userId);

        // 3. 토스 api 결제 요청
        TossPaymentConfirmResponse tossResponse = confirmPaymentWithTossApi(
            tossRequest);

        // 4. 결제 성공 처리 및 저장
        savePayment(tossResponse, user, order);
        user.increasePoint(tossResponse.totalAmount()); //사용자의 포인트 올려줌
        order.updateStatus(OrderStatus.DONE); //주문 완료 처리
        log.info("결제 금액 = {}", tossResponse.totalAmount());

        log.info("결제 처리 - userId={}, orderId={}",userId, order.getId());

        log.info("결제 처리 - userId={}, orderId={}",userId, order.getId());

        return new PaymentSuccessResponse(tossResponse.orderId(), tossResponse.totalAmount());

    }

    //토스 API 외부 요청
    private TossPaymentConfirmResponse confirmPaymentWithTossApi(
        TossPaymentConfirmRequest tossRequest) {
        //인증 헤더 생성
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        // Feign 요청
        return tossApiClient.confirmPayment("Basic " + encodedKey,
            tossRequest);
    }

    private void savePayment(TossPaymentConfirmResponse response, User user, Order order) {
        // 토스 api 응답이 DONE이 아닐 경우 결제 실패 처리
        if (response.status() != PaymentStatus.DONE) {
            order.updateStatus(OrderStatus.FAILED); //TODO: canceled 등 상세 구분이 필요한가?
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST); //400
        }

        paymentRepository.save(PaymentMapper.toEntity(response, user, order));
    }

    @Transactional(readOnly = true)
    public PaymentScrollResponse getPaymentHistories(int size, Long cursor, long userId) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "id"));
        Slice<Payment> payments = paymentRepository.findByUserIdAndCursor(userId, cursor, pageable);

        Long nextCursor = null;
        if (payments.hasNext()) {
            nextCursor = payments.getContent().getLast().getId();
        }
        List<PaymentHistoryResponse> content = payments.stream()
            .map(payment -> PaymentMapper.toPaymentHistoryResponse(payment))
            .toList();

        return new PaymentScrollResponse(content, nextCursor);
    }
}