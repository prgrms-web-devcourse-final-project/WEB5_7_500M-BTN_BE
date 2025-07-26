package shop.matjalalzz.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
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

    private User user;
    private Shop shop;
    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void outerSetUp() {
        user = TestUtil.createUser();
        shop = TestUtil.createShop(user);
        em.persist(user);
        em.persist(shop);
    }

    @Nested
    @DisplayName("findByShopIdWithFilterAndCursor")
    class findByShopIdWithFilterAndCursorTest {

        List<Reservation> baseline;   // 최신 id → 오래된 id
        Long cursor;

        @BeforeEach
        void setUpReservations() {
            baseline = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> {
                    Party party = TestUtil.createParty(shop);
                    em.persist(party);
                    LocalDateTime at = LocalDateTime.now().plusHours(i).withNano(0);
                    Reservation reservation = TestUtil.createReservation(shop, user, party, at);
                    em.persist(reservation);
                    return reservation;
                })
                .sorted(Comparator.comparingLong(Reservation::getId).reversed())
                .toList();
            em.flush();
            cursor = baseline.getFirst().getId();
            em.clear();
        }

        @Test
        @DisplayName("상태 = PENDING, cursor 기준 ID < cursor 필터링")
        void 상태_커서가_모두_주어진_경우() {
            Slice<Reservation> result = reservationRepository.findByShopIdWithFilterAndCursor(
                shop.getId(), ReservationStatus.PENDING, cursor, pageable);

            assertThat(result).extracting(Reservation::getId)
                .containsExactly(baseline.get(1).getId(), baseline.get(2).getId());
        }

        @Test
        @DisplayName("상태만 주어진 경우")
        void 상태만_주어진_경우() {
            Slice<Reservation> result = reservationRepository.findByShopIdWithFilterAndCursor(
                shop.getId(), ReservationStatus.PENDING, null, pageable);

            assertThat(result).extracting(Reservation::getId)
                .containsExactlyElementsOf(ids(baseline));
        }

        @Test
        @DisplayName("커서만 주어진 경우")
        void 커서만_주어진_경우() {
            Slice<Reservation> result = reservationRepository.findByShopIdWithFilterAndCursor(
                shop.getId(), null, null, pageable);

            assertThat(result).extracting(Reservation::getId)
                .containsExactlyElementsOf(ids(baseline));
        }

        @Test
        @DisplayName("상태, 커서 모두 없는 경우 (전체 조회)")
        void 상태_커서_모두_없는_경우() {
            Slice<Reservation> result = reservationRepository.findByShopIdWithFilterAndCursor(
                shop.getId(), null, null, pageable);

            assertThat(result).extracting(Reservation::getId)
                .containsExactlyElementsOf(ids(baseline));
        }

        private List<Long> ids(List<Reservation> list) {
            return list.stream().map(Reservation::getId).toList();
        }
    }

    @Nested
    @DisplayName("예약 저장 테스트")
    class ReservationSaveTest {

        @Test
        @DisplayName("예약 저장 성공")
        void 예약_저장_성공() {
            // given
            Party party = TestUtil.createParty(shop);
            em.persist(party);

            LocalDateTime reservedAt = LocalDateTime.now().plusHours(1).withNano(0);
            Reservation reservation = TestUtil.createReservation(shop, user, party, reservedAt);

            // when
            em.persist(reservation);
            em.flush();
            em.clear();

            // then
            Reservation found = em.find(Reservation.class, reservation.getId());
            assertThat(found).isNotNull();
            assertThat(found.getShop().getId()).isEqualTo(shop.getId());
            assertThat(found.getReservedAt()).isEqualTo(reservedAt);
        }

        @Test
        @DisplayName("중복 예약 여부 확인")
        void 중복_예약_존재_확인() {
            // given
            Party party = TestUtil.createParty(shop);
            em.persist(party);

            LocalDateTime reservedAt = LocalDateTime.now().plusHours(1).withNano(0);
            Reservation reservation = TestUtil.createReservation(shop, user, party, reservedAt);
            em.persist(reservation);
            em.flush();
            em.clear();

            // when
            boolean exists = reservationRepository.existsByShopIdAndReservationAt(shop.getId(),
                reservedAt);

            // then
            assertThat(exists).isTrue();
        }
    }
}