package shop.matjalalzz.tosspay.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.tosspay.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);
}
