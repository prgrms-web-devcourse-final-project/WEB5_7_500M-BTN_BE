package shop.matjalalzz.tosspay.dto;

/**
 * @param orderId     주문 번호
 * @param totalAmount 결제 금액
 */
public record PaymentSuccessResponse(
    String orderId,
    int totalAmount
) {

}