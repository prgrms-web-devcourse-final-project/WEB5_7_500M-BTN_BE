package shop.matjalalzz.tosspay.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "결제 API", description = "토스Payment API")
// Toss 결제 성공 후 백엔드에서 결제를 최종 승인하고 포인트 충전
public class PaymentController {

    private final PaymentService paymentService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/confirm")
    @Operation(summary = "Confirm", description = """
        TOSS_SECRET_KEY=test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6
        TOSS_CLIENT_KEY=test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm
        혹여나 프론트와 토스페이 api 통신이 되지 않으면  https://github.com/prgrms-be-devcourse/NBE5-7-2-Team09  링크에 프론트 코드 링크 참조해주세요""")
    public BaseResponse<PaymentSuccessResponse> confirm(
        @RequestBody @Valid TossPaymentConfirmRequest request,
        @AuthenticationPrincipal PrincipalUser principalUser) {
        PaymentSuccessResponse response = paymentService.confirmPayment(request,
            principalUser.getId());
        return BaseResponse.ok(response, BaseStatus.OK);
    }
}