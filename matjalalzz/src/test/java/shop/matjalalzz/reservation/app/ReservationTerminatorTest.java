package shop.matjalalzz.reservation.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.util.TestUtil;

@SpringBootTest
@Transactional
@DisplayName("예약 Scheduler 배치 테스트")
class ReservationSchedulerTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Test
    @DisplayName("CONFIRMED 상태이면서 reservedAt + 1일이 지난 예약은 TERMINATED 처리된다.")
    void terminateExpiredConfirmedReservations() {
        // given
        User user = userRepository.save(TestUtil.createUser());
        Shop shop = shopRepository.save(TestUtil.createShop(user));

        Reservation expired1 = reservationRepository.save(
            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusDays(2))
        );
        expired1.changeStatus(ReservationStatus.CONFIRMED);

        Reservation expired2 = reservationRepository.save(
            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusDays(1).minusMinutes(5))
        );
        expired2.changeStatus(ReservationStatus.CONFIRMED);

        // 하루가 아직 안 지난 예약 경우
        Reservation valid = reservationRepository.save(
            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusHours(23))
        );
        valid.changeStatus(ReservationStatus.CONFIRMED);

        // 이미 TERMINATED 처리된 예약 경우
        Reservation alreadyTerminated = reservationRepository.save(
            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusDays(5))
        );
        alreadyTerminated.changeStatus(ReservationStatus.TERMINATED);

        // when
        int result = reservationService.terminateExpiredReservations();

        // then
        assertThat(result).isEqualTo(2);

        assertThat(reservationRepository.findById(expired1.getId()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.TERMINATED);
        assertThat(reservationRepository.findById(expired2.getId()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.TERMINATED);
        assertThat(reservationRepository.findById(valid.getId()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservationRepository.findById(alreadyTerminated.getId()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.TERMINATED);
    }

    @Test
    @DisplayName("PENDING 상태이면서 reservedAt + 1시간이 지난 예약은 REFUSED 처리된다.")
    void refuseExpiredPendingReservations() {
        // given
        User user = userRepository.save(TestUtil.createUser());
        Shop shop = shopRepository.save(TestUtil.createShop(user));

        Reservation expired1 = reservationRepository.save(
            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusHours(2))
        );

        Reservation expired2 = reservationRepository.save(
            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusHours(1).minusMinutes(5))
        );

        // 아직 1시간이 지나지 않은 경우
        Reservation valid = reservationRepository.save(
            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusMinutes(30))
        );

        // 이미 REFUSED 처리된 예약
        Reservation alreadyRefused = reservationRepository.save(
            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusHours(3))
        );
        alreadyRefused.changeStatus(ReservationStatus.REFUSED);

        // when
        int result = reservationService.refuseExpiredPendingReservations();

        // then
        assertThat(result).isEqualTo(2);

        assertThat(reservationRepository.findById(expired1.getId()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.REFUSED);
        assertThat(reservationRepository.findById(expired2.getId()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.REFUSED);
        assertThat(reservationRepository.findById(valid.getId()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.PENDING);
        assertThat(reservationRepository.findById(alreadyRefused.getId()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.REFUSED);
    }
}

