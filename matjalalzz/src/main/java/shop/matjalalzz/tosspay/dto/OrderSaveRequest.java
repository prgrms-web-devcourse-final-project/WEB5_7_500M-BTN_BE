package shop.matjalalzz.tosspay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderSaveRequest(
    @NotBlank @Schema(description = "클라이언트에서 생성한 주문 id") String orderId,
    @Min(value=1000) @Schema(name = "결제 금액", description = "결제 금액은 포인트 충전 머니입니다.. 1000원부터 1000원 단위로 50000원까지 충전 가능합니다.") int amount
) {

}
