package shop.matjalalzz.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static shop.matjalalzz.util.TestUtil.createShop;
import static shop.matjalalzz.util.TestUtil.createUser;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.util.TestUtil;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EntityManager em;
    
    @Test
    @DisplayName("예약이 존재하면 true를 반환한다")
    void existsReservation_shouldReturnTrue() throws Exception {
        // given
        User user = createUser();
        Shop shop = createShop(user);
        LocalDateTime reservedAt = LocalDateTime.of(2025, 7, 15, 18, 0);

        Reservation reservation = TestUtil.createReservation(shop, user, null, reservedAt);
        reservationRepository.save(reservation);

        em.flush();
        em.clear();

        // when
        boolean exists = reservationRepository.existsByShopIdAndReservationAt(shop.getId(), reservedAt);

        // then
        assertThat(exists).isTrue();

    }

}