package shop.matjalalzz.tosspay.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.tosspay.app.OrderService;
import shop.matjalalzz.tosspay.dto.OrderSaveRequest;
import shop.matjalalzz.tosspay.dto.PaymentScrollResponse;
import shop.matjalalzz.tosspay.dto.PaymentSuccessResponse;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmRequest;
import shop.matjalalzz.tosspay.app.PaymentService;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "토스 결제 API", description = """
        TOSS_SECRET_KEY=test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6
        TOSS_CLIENT_KEY=test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm
        위 key 모두 테스트용으로 토스에서 발급해주는 key라 노출은 신경쓰지 않아도 됩니다.
        https://github.com/tosspayments/tosspayments-sample 여기에 결제 위젯에 대한 샘플 코드가 제공되어 있으니 참고해주세요.
        WidgetCheckout, WidgetSuccess, Fail 쪽을 참고해주시면 될 것 같습니다.
        """)
// Toss 결제 성공 후 백엔드에서 결제를 최종 승인하고 포인트 충전
public class PaymentController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping("/order")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "주문 정보 임시 저장 api", description = """
        1. 결제 요청 전에 주문 정보를 서버에 저장
           결제하기 버튼을 누를 때 (실제 결제 요청 api 보내기 전) 호출하면 됩니다. orderId는 클라이언트에서 임의의 랜덤한 숫자로 제작합니다.
        2. orderId, amount 저장
        3. 이후 결제 요청 / 결제 성공 시 서버에 저장된 값과 비교
         → 변조 방지, 악의적인 금액 조작 차단
         (Completed)
        """)
    public BaseResponse<Void> saveOrder(@RequestBody @Valid OrderSaveRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {
        orderService.saveOrder(request, principal.getId());
//        orderService.saveOrder(request, 1L); //테스트용
        return BaseResponse.ok(BaseStatus.CREATED);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("payment/confirm")
    @Operation(summary = "결제 승인 api ", description = """
        결제 성공 시 (WidgetSuccess로 이동할 때) 이 api를 호출해서 최종 결제 승인 및 저장을 실행합니다.
        이 api 에서 에러가 발생할 시 fail path로 리다이렉트합니다.
        (Completed)
        """)
    public BaseResponse<PaymentSuccessResponse> confirm(
        @RequestBody @Valid TossPaymentConfirmRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {
        PaymentSuccessResponse response = paymentService.confirmPayment(request,
            principal.getId());

//        PaymentSuccessResponse response = paymentService.confirmPayment(request,
//            1L); //테스트용
        return BaseResponse.ok(response, BaseStatus.CREATED);
    }

    @GetMapping("/payment")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "결제 내역 조회 api", description = "결제 내역을 스크롤 방식으로 조회합니다. 사용자의 마이페이지 란에서 조회 가능합니다. (Completed)")
    public BaseResponse<PaymentScrollResponse> getPaymentHistories(
        @RequestParam(required = false, defaultValue = "10") int size,
        @RequestParam(required = false) Long cursor,
        @AuthenticationPrincipal PrincipalUser principal) {
        PaymentScrollResponse response = paymentService.getPaymentHistories(size, cursor,
            principal.getId());
//        PaymentScrollResponse response = paymentService.getPaymentHistories(size, cursor,
//            1L); //테스트용
        return BaseResponse.ok(response,BaseStatus.OK);
    }


}