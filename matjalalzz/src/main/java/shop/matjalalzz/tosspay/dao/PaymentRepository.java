package shop.matjalalzz.tosspay.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.tosspay.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND (p.id < :cursor OR :cursor IS NULL)")
    Slice<Payment> findByUserIdAndCursor(@Param("userId") long userId, @Param("cursor") Long cursor,
        Pageable pageable);
}
