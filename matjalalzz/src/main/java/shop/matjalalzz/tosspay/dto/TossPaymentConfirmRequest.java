package shop.matjalalzz.tosspay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TossPaymentConfirmRequest(
    @NotBlank(message = "결제 키(paymentKey)는 필수 입력값입니다.") @Schema(description = "결제 성공 시 토스에서 쿼리 파라미터로 반환해주는 paymentKey") String paymentKey,

    @NotBlank(message = "주문 번호(orderId)는 필수 입력값입니다.") @Schema(description = "클라이언트에서 생성한 주문 id") String orderId,

    @Min(value = 1000) @Schema(description = "결제 금액(1000~50000원)") int amount) {

}
