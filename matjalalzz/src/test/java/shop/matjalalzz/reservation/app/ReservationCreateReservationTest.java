package shop.matjalalzz.reservation.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.util.TestUtil;

@SpringBootTest
@Transactional
@Rollback
@DisplayName("ReservationService 예약 생성 테스트")
class ReservationCreateReservationTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    PartyRepository partyRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Nested
    @DisplayName("createReservation 메서드")
    class CreateReservationTest {

        @Test
        @DisplayName("party가 있는 경우 예약 생성 성공")
        void createReservationWithParty() {
            // given
            User user = userRepository.save(TestUtil.createUser());
            Shop shop = shopRepository.save(TestUtil.createShop(user));
            Party party = partyRepository.save(TestUtil.createParty(shop));
            CreateReservationRequest request = new CreateReservationRequest("2025-07-15", "19:00", 2, 10000);

            // when
            CreateReservationResponse response = reservationService.createReservation(
                user.getId(), shop.getId(), party.getId(), request
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.shopName()).isEqualTo(shop.getName());
            assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
        }

        @Test
        @DisplayName("party가 없는 경우 예약 생성 성공")
        void createReservationWithoutParty() {
            // given
            User user = userRepository.save(TestUtil.createUser());
            Shop shop = shopRepository.save(TestUtil.createShop(user));
            CreateReservationRequest request = new CreateReservationRequest("2025-07-15", "20:00", 2, 10000);

            // when
            CreateReservationResponse response = reservationService.createReservation(
                user.getId(), shop.getId(), null, request
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.shopName()).isEqualTo(shop.getName());
            assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
        }

        @Test
        @DisplayName("중복 예약 시간인 경우 예외 발생")
        void duplicateReservationTime() {
            // given
            User user = userRepository.save(TestUtil.createUser());
            Shop shop = shopRepository.save(TestUtil.createShop(user));
            Party party = partyRepository.save(TestUtil.createParty(shop));

            LocalDateTime reservedAt = LocalDateTime.of(2025, 7, 15, 21, 0);
            reservationRepository.save(TestUtil.createReservation(shop, user, party, reservedAt));

            CreateReservationRequest request = new CreateReservationRequest("2025-07-15", "21:00", 2, 10000);

            // when & then
            assertThatThrownBy(() -> reservationService.createReservation(
                user.getId(), shop.getId(), party.getId(), request
            )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("데이터가 중복 되었습니다.");
        }


        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @Nested
        @DisplayName("동시성 테스트")
        class ConcurrencyTest {

            @Commit
            @Test
            @DisplayName("동일한 예약 시간에 두 명이 동시에 예약하면 한 명만 성공")
            void concurrentReservationTest() throws InterruptedException {
                // given
                User user1 = userRepository.save(TestUtil.createUser());
                User user2 = userRepository.save(TestUtil.createUser());

                Shop shop = shopRepository.save(TestUtil.createShop(user1));
                String date = "2025-07-15";
                String time = "20:00";
                CreateReservationRequest request = new CreateReservationRequest(date, time, 2, 10000);

                // when
                ExecutorService executor = Executors.newFixedThreadPool(2);
                CountDownLatch latch = new CountDownLatch(2);

                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failCount = new AtomicInteger(0);

                Function<Long, Runnable> reservationTask = (userId) -> () -> {
                    try {
                        Thread.sleep(100);
                        reservationService.createReservation(userId, shop.getId(), null, request);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        System.out.println("예약 실패 사용자 ID: " + userId + " / 예외: " + e.getMessage()); // 🔥 로그 추가
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                };

                executor.execute(reservationTask.apply(user1.getId()));
                executor.execute(reservationTask.apply(user2.getId()));

                latch.await(); // 두 스레드 완료 대기
                executor.shutdown();

                // then
                assertThat(reservationRepository.findAll()).hasSize(1);
                assertThat(successCount.get()).isEqualTo(1);
                assertThat(failCount.get()).isEqualTo(1);
            }
        }
    }
}

