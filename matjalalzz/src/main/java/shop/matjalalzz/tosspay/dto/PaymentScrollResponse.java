package shop.matjalalzz.tosspay.dto;

import java.util.List;

public record PaymentScrollResponse(
    List<PaymentHistoryResponse> content,
    Long nextCursor
) {

}
