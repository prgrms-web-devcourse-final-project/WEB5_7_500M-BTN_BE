package shop.matjalalzz.tosspay.dto;

public record OrderSaveRequest(
    String orderId,
    int amount
) {

}
