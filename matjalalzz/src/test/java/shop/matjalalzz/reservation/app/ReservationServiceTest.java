package shop.matjalalzz.reservation.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.util.ReflectionTestUtils;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ShopRepository shopRepository;

    private final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
    private final Long SHOP_ID = 1L;
    private final Long CURSOR = 3L;

    @Nested
    @DisplayName("getReservations - 예약 조회")
    class GetReservations {

        @Test
        @DisplayName("상태와 커서 모두 주어진 경우")
        void 상태와_커서_모두_주어진_경우() {
            // given
            User user = TestUtil.createUser();
            Shop shop = TestUtil.createShop(user);
            Party party = TestUtil.createParty(shop);

            Reservation r1 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(1));
            ReflectionTestUtils.setField(r1, "id", 1L);
            Reservation r2 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(2));
            ReflectionTestUtils.setField(r2, "id", 2L);

            List<Reservation> reservations = List.of(r2, r1);
            Slice<Reservation> slice = new SliceImpl<>(reservations, pageable, true);

            when(reservationRepository.findByShopIdWithFilterAndCursor(
                SHOP_ID,
                ReservationStatus.PENDING,
                CURSOR,
                pageable
            )).thenReturn(slice);

            // when
            ReservationListResponse result = reservationService.getReservations(
                SHOP_ID, "PENDING", CURSOR, 10
            );

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.nextCursor()).isEqualTo(1L);
        }

        @Test
        @DisplayName("status만 주어진 경우 - 해당 상태의 예약 목록 전체 반환")
        void 상태만_주어진_경우() {
            // given
            User user = TestUtil.createUser();
            Shop shop = TestUtil.createShop(user);
            Party party = TestUtil.createParty(shop);

            Reservation r1 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(1));
            ReflectionTestUtils.setField(r1, "id", 1L);
            Reservation r2 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(2));
            ReflectionTestUtils.setField(r2, "id", 2L);

            List<Reservation> reservations = List.of(r2, r1);
            Slice<Reservation> slice = new SliceImpl<>(reservations, pageable, true);

            when(reservationRepository.findByShopIdWithFilterAndCursor(
                SHOP_ID,
                ReservationStatus.PENDING,
                null,
                pageable
            )).thenReturn(slice);

            // when
            ReservationListResponse result = reservationService.getReservations(
                SHOP_ID, "PENDING", null, 10
            );

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.nextCursor()).isEqualTo(1L);
        }

        @Test
        @DisplayName("cursor 기준으로 ID < cursor인 예약들만 조회")
        void 커서만_주어진_경우() {
            // given
            User user = TestUtil.createUser();
            Shop shop = TestUtil.createShop(user);
            Party party = TestUtil.createParty(shop);

            Reservation r1 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(2));
            Reservation r3 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(3));
            List<Reservation> reservations = List.of(r3, r2, r1);
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Direction.DESC, "id"));
            Slice<Reservation> slice = new SliceImpl<>(List.of(r3, r2), pageable, true);

            given(reservationRepository.findByShopIdWithFilterAndCursor(shop.getId(), null,
                r3.getId(), pageable))
                .willReturn(slice);

            // when
            ReservationListResponse response = reservationService.getReservations(
                shop.getId(),
                null,
                r3.getId(),
                2
            );

            // then
            assertThat(response.nextCursor()).isEqualTo(r2.getId());
            assertThat(response.content()).hasSize(2);
            assertThat(response.content())
                .extracting("reservationId")
                .containsExactly(r3.getId(), r2.getId());
        }

        @Test
        @DisplayName("shopId 기준 전체 예약을 최신순으로 조회")
        void 상태_커서_모두_없는_경우() {
            // given
            User user = TestUtil.createUser();
            Shop shop = TestUtil.createShop(user);
            Party party = TestUtil.createParty(shop);

            Reservation r1 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(1)); // ID 1
            Reservation r2 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(2)); // ID 2
            Reservation r3 = TestUtil.createReservation(shop, user, party,
                LocalDateTime.now().plusHours(3)); // ID 3

            List<Reservation> reservations = List.of(r3, r2, r1); // 최신순
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Direction.DESC, "id"));
            Slice<Reservation> slice = new SliceImpl<>(List.of(r3, r2), pageable, true);

            given(reservationRepository.findByShopIdWithFilterAndCursor(shop.getId(), null, null,
                pageable))
                .willReturn(slice);

            // when
            ReservationListResponse response = reservationService.getReservations(
                shop.getId(),
                null,
                null,
                2
            );

            // then
            assertThat(response.nextCursor()).isEqualTo(r2.getId());
            assertThat(response.content()).hasSize(2);
            assertThat(response.content())
                .extracting("reservationId")
                .containsExactly(r3.getId(), r2.getId());
        }
    }

    @Nested
    @DisplayName("confirmReservation / cancelReservation - 예약 상태 변경")
    class UpdateReservationStatus {

        @Test
        @DisplayName("예약 수락 성공")
        void 예약_수락_성공() {
            // given
            User user = TestUtil.createUser();
            Shop shop = TestUtil.createShop(user);
            Reservation reservation = TestUtil.createReservation(shop, user, null,
                LocalDateTime.now());
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(shop, "id", 1L);
            ReflectionTestUtils.setField(reservation, "id", 1L);
            reservation.changeStatus(ReservationStatus.PENDING);

            given(reservationRepository.findById(1L)).willReturn(
                java.util.Optional.of(reservation));

            // when
            reservationService.confirmReservation(shop.getId(), reservation.getId(), user.getId());

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        }

        @Test
        @DisplayName("예약 거절 성공")
        void 예약_거절_성공() {
            // given
            User user = TestUtil.createUser();
            Shop shop = TestUtil.createShop(user);
            Reservation reservation = TestUtil.createReservation(shop, user, null,
                LocalDateTime.now());
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(shop, "id", 1L);
            ReflectionTestUtils.setField(reservation, "id", 2L);
            reservation.changeStatus(ReservationStatus.PENDING);

            given(reservationRepository.findById(2L)).willReturn(
                java.util.Optional.of(reservation));

            // when
            reservationService.cancelReservation(shop.getId(), reservation.getId(), user.getId());

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        }

        @Test
        @DisplayName("이미 처리된 예약 수락 시 예외")
        void 예약_수락_실패_이미처리됨() {
            // given
            User user = TestUtil.createUser();
            Shop shop = TestUtil.createShop(user);
            Reservation reservation = TestUtil.createReservation(shop, user, null,
                LocalDateTime.now());
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(shop, "id", 1L);
            ReflectionTestUtils.setField(reservation, "id", 3L);
            reservation.changeStatus(ReservationStatus.CONFIRMED); // 이미 수락됨

            given(reservationRepository.findById(3L)).willReturn(Optional.of(reservation));

            // when & then
            assertThrows(BusinessException.class, () ->
                reservationService.confirmReservation(shop.getId(), reservation.getId(),
                    user.getId())
            );
        }

        @Test
        @DisplayName("이미 처리된 예약 거절 시 예외")
        void 예약_거절_실패_이미처리됨() {
            // given
            User user = TestUtil.createUser();
            ReflectionTestUtils.setField(user, "id", 1L);

            Shop shop = TestUtil.createShop(user);
            ReflectionTestUtils.setField(shop, "id", 1L);

            Reservation reservation = TestUtil.createReservation(shop, user, null,
                LocalDateTime.now());
            ReflectionTestUtils.setField(reservation, "id", 4L);
            reservation.changeStatus(ReservationStatus.CANCELLED); // 이미 거절됨

            given(reservationRepository.findById(4L)).willReturn(Optional.of(reservation));

            // when & then
            assertThrows(BusinessException.class, () ->
                reservationService.cancelReservation(shop.getId(), reservation.getId(),
                    user.getId())
            );
        }
    }


}
