package shop.matjalalzz.tosspay.app;


import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.tosspay.config.TossApiClient;
import shop.matjalalzz.tosspay.dto.PaymentSuccessResponse;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmRequest;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmResponse;
import shop.matjalalzz.tosspay.entity.Order;
import shop.matjalalzz.tosspay.entity.OrderStatus;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossApiClient tossApiClient;
    private final UserService userService;
    private final OrderService orderService;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Transactional
    public PaymentSuccessResponse confirmPayment(TossPaymentConfirmRequest tossRequest,
        Long userId) {

        // 1. 유저 검증
        User user = userService.getUserById(userId);

        // 2. 결제 데이터 무결성 확인
        Order order = orderService.validateOrder(tossRequest);

        // 3. 토스 api 결제 요청
        confirmPaymentWithTossApi(tossRequest);

        // 4. 결제 성공 처리
        user.updatePoint(tossRequest.amount()); //사용자의 포인트 올려줌
        order.updateStatus(OrderStatus.DONE); //주문 완료 처리

        //TODO: 객체 생성 후 반환 코드 필요
        return PaymentSuccessResponse.builder()
            .orderId(tossRequest.orderId())
            .amount(tossRequest.amount()).build();

    }

    //토스 API 외부 요청
    private void confirmPaymentWithTossApi(TossPaymentConfirmRequest tossRequest) {
        //인증 헤더 생성
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        // Feign 요청
        TossPaymentConfirmResponse response = tossApiClient.confirmPayment("Basic " + encodedKey,
            tossRequest);

        // 토스 api 응답이 DONE이 아닐 경우 결제 실패 처리
        if (!"DONE".equals(response.status())) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_REQUEST); //400
        }
    }
}