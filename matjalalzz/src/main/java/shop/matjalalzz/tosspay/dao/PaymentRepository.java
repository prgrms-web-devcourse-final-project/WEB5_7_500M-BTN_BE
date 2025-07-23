package shop.matjalalzz.tosspay.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.tosspay.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
