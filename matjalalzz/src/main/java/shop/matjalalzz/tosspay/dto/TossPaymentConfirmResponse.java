package shop.matjalalzz.tosspay.dto;

import shop.matjalalzz.tosspay.entity.PaymentStatus;

public record TossPaymentConfirmResponse(
    String orderId, //주문번호
    String paymentKey, //결제 키값 (결제 승인, 조회, 취소 api에서 사용)
    String method, //결제 수단
    int totalAmount, //결제 금액
    PaymentStatus status //결제 상태
) {

}
