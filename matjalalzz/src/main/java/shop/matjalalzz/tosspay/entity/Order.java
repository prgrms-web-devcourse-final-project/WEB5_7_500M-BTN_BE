package shop.matjalalzz.tosspay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId; // 클라이언트에서 보내주는 주문 ID

    @Column(nullable = false)
    private int amount; // 결제 금액

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder
    public Order(String orderId, int amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = OrderStatus.READY;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

}
