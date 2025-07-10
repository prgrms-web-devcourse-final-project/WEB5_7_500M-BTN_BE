package shop.matjalalzz.tosspay.app;


import java.util.Base64;
import java.util.Optional;
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
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossApiClient tossApiClient;
    private final UserRepository userRepository;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Transactional
    public PaymentSuccessResponse confirmPayment(TossPaymentConfirmRequest tossRequest,
        Long userId) {

        //유저 검증
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND); //404
        }

        //인증 헤더 생성
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        // Feign 요청
        TossPaymentConfirmResponse response = tossApiClient.confirmPayment("Basic " + encodedKey,
            tossRequest);

        if (!"DONE".equals(response.status())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA); //400
        }

        User user = userOptional.get();
        int point = tossRequest.amount(); //int로 바꿨는데 에러 없는지 확인 필요

        if (point < 1) {
            throw new BusinessException(ErrorCode.ZERO_AMOUNT_PAYMENT_NOT_ALLOWED); // 0원 결제 시 400
        }
        user.updatePoint(point);

        //객체 생성 후 반환 코드 필요
        PaymentSuccessResponse Response = PaymentSuccessResponse.builder()
            .orderId(tossRequest.orderId())
            .amount(tossRequest.amount()).build();

        return Response;
    }
}