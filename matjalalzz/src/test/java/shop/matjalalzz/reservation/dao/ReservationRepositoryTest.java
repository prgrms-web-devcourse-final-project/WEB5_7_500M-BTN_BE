package shop.matjalalzz.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.util.TestUtil;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EntityManager em;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("findByShopIdWithFilterAndCursor")
    class findByShopIdWithFilterAndCursorTest {

        @Test
        @DisplayName("상태 = PENDING, cursor 기준 ID < cursor 필터링")
        void 상태_커서가_모두_주어진_경우() {

            User user = TestUtil.createUser();
            em.persist(user);

            Shop shop = TestUtil.createShop(user);
            em.persist(shop);

            Party party = TestUtil.createParty(shop);
            em.persist(party);

            em.flush();

            // reservation 생성
            Reservation r1 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(2));
            Reservation r3 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(3));

            em.persist(r1);
            em.persist(r2);
            em.persist(r3);
            em.flush(); // id 할당

            Long cursor = r3.getId();
            em.clear();

            Slice<Reservation> result = reservationRepository.findByShopIdWithFilterAndCursor(
                shop.getId(),
                ReservationStatus.PENDING,
                cursor,
                pageable
            );

            assertThat(result).hasSize(2);
            assertThat(result).extracting("id").containsExactly(r2.getId(), r1.getId());
        }

        @Test
        @DisplayName("상태만 주어진 경우")
        void 상태만_주어진_경우() {
            User user = TestUtil.createUser();
            em.persist(user);

            Shop shop = TestUtil.createShop(user);
            em.persist(shop);

            Party party = TestUtil.createParty(shop);
            em.persist(party);

            em.flush();

            // reservation 생성
            Reservation r1 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(2));
            Reservation r3 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(3));

            em.persist(r1);
            em.persist(r2);
            em.persist(r3);
            em.flush(); // id 할당

            Slice<Reservation> result = reservationRepository.findByShopIdWithFilterAndCursor(
                shop.getId(),
                ReservationStatus.PENDING,
                null,
                pageable
            );

            assertThat(result).hasSize(2);
            assertThat(result).extracting("id").containsExactly(r2.getId(), r1.getId());
        }

        @Test
        @DisplayName("커서만 주어진 경우")
        void 커서만_주어진_경우() {
            User user = TestUtil.createUser();
            em.persist(user);

            Shop shop = TestUtil.createShop(user);
            em.persist(shop);

            Party party = TestUtil.createParty(shop);
            em.persist(party);

            em.flush();

            Reservation r1 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(2));
            Reservation r3 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(3));

            em.persist(r1);
            em.persist(r2);
            em.persist(r3);
            em.flush();

            Long cursor = r3.getId();
            em.clear();

            Slice<Reservation> result = reservationRepository.findByShopIdWithFilterAndCursor(
                shop.getId(),
                null,
                cursor,
                pageable
            );

            assertThat(result).hasSize(2);
            assertThat(result).extracting("id").containsExactly(r2.getId(), r1.getId());
        }

        @Test
        @DisplayName("상태, 커서 모두 없는 경우 (전체 조회)")
        void 상태_커서_모두_없는_경우() {
            User user = TestUtil.createUser();
            em.persist(user);

            Shop shop = TestUtil.createShop(user);
            em.persist(shop);

            Party party = TestUtil.createParty(shop);
            em.persist(party);

            em.flush();

            Reservation r1 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(2));
            Reservation r3 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(3));

            em.persist(r1);
            em.persist(r2);
            em.persist(r3);
            em.flush();

            em.clear();

            Slice<Reservation> result = reservationRepository.findByShopIdWithFilterAndCursor(
                shop.getId(),
                null,
                null,
                pageable
            );

            assertThat(result).hasSize(3);
            assertThat(result).extracting("id").containsExactly(r3.getId(), r2.getId(), r1.getId());
        }

    }
}