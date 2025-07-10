package shop.matjalalzz.tosspay.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.tosspay.app.PaymentService;
import shop.matjalalzz.tosspay.dto.PaymentSuccessResponse;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Validated
// Toss 결제 성공 후 백엔드에서 결제를 최종 승인하고 포인트 충전
public class PaymentController {

    private final PaymentService paymentService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/confirm")
    public BaseResponse<PaymentSuccessResponse> confirm(
        @RequestBody @Valid TossPaymentConfirmRequest request,
        @AuthenticationPrincipal PrincipalUser principalUser) {
        PaymentSuccessResponse response = paymentService.confirmPayment(request,
            principalUser.getId());
        return BaseResponse.ok(response, BaseStatus.OK);
    }
}