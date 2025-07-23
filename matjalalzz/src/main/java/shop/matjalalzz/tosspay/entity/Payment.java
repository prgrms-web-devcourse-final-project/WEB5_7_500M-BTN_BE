package shop.matjalalzz.tosspay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.matjalalzz.global.common.BaseEntity;
import shop.matjalalzz.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    private String method;

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String paymentKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder
    public Payment(String method, int totalAmount, PaymentStatus status, String paymentKey,
        User user,
        Order order) {
        this.method = method;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentKey = paymentKey;
        this.user = user;
        this.order = order;
    }
}
