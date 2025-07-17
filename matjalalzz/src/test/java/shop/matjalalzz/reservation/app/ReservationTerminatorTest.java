//package shop.matjalalzz.reservation.app;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.time.LocalDateTime;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//import shop.matjalalzz.reservation.dao.ReservationRepository;
//import shop.matjalalzz.reservation.entity.Reservation;
//import shop.matjalalzz.reservation.entity.ReservationStatus;
//import shop.matjalalzz.shop.dao.ShopRepository;
//import shop.matjalalzz.shop.entity.Shop;
//import shop.matjalalzz.user.dao.UserRepository;
//import shop.matjalalzz.user.entity.User;
//import shop.matjalalzz.util.TestUtil;
//
//@SpringBootTest
//@Transactional
//@DisplayName("예약 TERMINATE 배치 테스트")
//class ReservationTerminatorTest {
//
//    @Autowired
//    private ReservationRepository reservationRepository;
//
//    @Autowired
//    private ReservationService reservationService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ShopRepository shopRepository;
//
//    @Test
//    @DisplayName("reservedAt + 1일이 지난 예약은 TERMINATED 처리된다.")
//    void terminateExpiredReservations() {
//        // given
//        User user = userRepository.save(TestUtil.createUser());
//        Shop shop = shopRepository.save(TestUtil.createShop(user));
//
//        // TERMINATE 대상
//        Reservation expired1 = reservationRepository.save(
//            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusDays(2))
//        );
//
//        Reservation expired2 = reservationRepository.save(
//            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusDays(1).minusMinutes(5))
//        );
//
//        // TERMINATE 대상 아님
//        Reservation valid = reservationRepository.save(
//            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusHours(23))
//        );
//
//        // TERMINATED 상태로 이미 있는 예약
//        Reservation alreadyTerminated = reservationRepository.save(
//            TestUtil.createReservation(shop, user, null, LocalDateTime.now().minusDays(5))
//        );
//        alreadyTerminated.changeStatus(ReservationStatus.TERMINATED); // 상태 직접 설정
//
//        // when
//        int result = reservationService.terminateExpiredReservations();
//
//        // then
//        assertThat(result).isEqualTo(2);
//
//        Reservation check1 = reservationRepository.findById(expired1.getId()).orElseThrow();
//        Reservation check2 = reservationRepository.findById(expired2.getId()).orElseThrow();
//        Reservation check3 = reservationRepository.findById(valid.getId()).orElseThrow();
//        Reservation check4 = reservationRepository.findById(alreadyTerminated.getId()).orElseThrow();
//
//        assertThat(check1.getStatus()).isEqualTo(ReservationStatus.TERMINATED);
//        assertThat(check2.getStatus()).isEqualTo(ReservationStatus.TERMINATED);
//        assertThat(check3.getStatus()).isEqualTo(ReservationStatus.PENDING); // 유지
//        assertThat(check4.getStatus()).isEqualTo(ReservationStatus.TERMINATED); // 유지
//    }
//}
//
