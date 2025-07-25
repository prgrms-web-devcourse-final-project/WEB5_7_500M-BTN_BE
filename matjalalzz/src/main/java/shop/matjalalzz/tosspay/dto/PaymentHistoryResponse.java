package shop.matjalalzz.tosspay.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import shop.matjalalzz.tosspay.entity.PaymentStatus;

@Builder
public record PaymentHistoryResponse(
    Long paymentId,
    String method,
    int totalAmount,
    PaymentStatus status,
    LocalDateTime createdAt
) {

}
