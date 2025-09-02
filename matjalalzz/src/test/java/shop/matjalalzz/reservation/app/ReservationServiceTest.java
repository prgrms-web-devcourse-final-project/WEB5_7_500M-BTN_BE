package shop.matjalalzz.reservation.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

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
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationFacade reservationFacade;

    @Mock
    private ShopService shopService;

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
            User owner = TestUtil.createUser();
            ReflectionTestUtils.setField(owner, "id", 1L);

            Shop shop = TestUtil.createShop(owner);
            ReflectionTestUtils.setField(shop, "id", SHOP_ID);

            Reservation r1 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(2));
            ReflectionTestUtils.setField(r1, "id", 1L);
            ReflectionTestUtils.setField(r2, "id", 2L);

            List<Reservation> reservations = List.of(r2, r1);
            Slice<Reservation> slice = new SliceImpl<>(reservations, pageable, true);

            // mocking
            given(shopService.shopFind(SHOP_ID)).willReturn(shop);
            given(reservationFacade.findByShopIdWithFilterAndCursor(SHOP_ID,
                ReservationStatus.PENDING, CURSOR, pageable))
                .willReturn(slice);

            // when
            ReservationListResponse result = reservationFacade.getReservations(
                SHOP_ID, ReservationStatus.PENDING, owner.getId(), CURSOR, 10
            );

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.nextCursor()).isEqualTo(1L);
        }

        @Test
        @DisplayName("상태만 주어진 경우 - 해당 상태의 예약 목록 전체 반환")
        void 상태만_주어진_경우() {
            // given
            User owner = TestUtil.createUser();
            ReflectionTestUtils.setField(owner, "id", 1L);

            Shop shop = TestUtil.createShop(owner);
            ReflectionTestUtils.setField(shop, "id", SHOP_ID);

            Reservation r1 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(2));
            ReflectionTestUtils.setField(r1, "id", 1L);
            ReflectionTestUtils.setField(r2, "id", 2L);

            List<Reservation> reservations = List.of(r2, r1);
            Slice<Reservation> slice = new SliceImpl<>(reservations, pageable, true);

            // mocking
            given(shopService.shopFind(SHOP_ID)).willReturn(shop);
            given(reservationFacade.findByShopIdWithFilterAndCursor(SHOP_ID,
                ReservationStatus.PENDING, null, pageable))
                .willReturn(slice);

            // when
            ReservationListResponse result = reservationFacade.getReservations(
                SHOP_ID, ReservationStatus.PENDING, owner.getId(), null, 10
            );

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.nextCursor()).isEqualTo(1L);
        }

        @Test
        @DisplayName("cursor 기준으로 ID < cursor인 예약들만 조회")
        void 커서만_주어진_경우() {
            // given
            User owner = TestUtil.createUser();
            ReflectionTestUtils.setField(owner, "id", 1L);

            Shop shop = TestUtil.createShop(owner);
            ReflectionTestUtils.setField(shop, "id", SHOP_ID);

            Reservation r1 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(2));
            Reservation r3 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(3));
            ReflectionTestUtils.setField(r1, "id", 1L);
            ReflectionTestUtils.setField(r2, "id", 2L);
            ReflectionTestUtils.setField(r3, "id", 3L);

            Pageable pageable = PageRequest.of(0, 2, Sort.by(Direction.DESC, "id"));
            Slice<Reservation> slice = new SliceImpl<>(List.of(r2, r1), pageable,
                true); // r3는 커서 기준으로 제외됨

            // mocking
            given(shopService.shopFind(SHOP_ID)).willReturn(shop);
            given(
                reservationFacade.findByShopIdWithFilterAndCursor(SHOP_ID, null, 3L, pageable))
                .willReturn(slice);

            // when
            ReservationListResponse result = reservationFacade.getReservations(
                SHOP_ID, null, owner.getId(), 3L, 2
            );

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.nextCursor()).isEqualTo(1L); // 마지막 ID가 1이므로
        }

        @Test
        @DisplayName("shopId 기준 전체 예약을 최신순으로 조회")
        void 상태_커서_모두_없는_경우() {
            // given
            User owner = TestUtil.createUser();
            ReflectionTestUtils.setField(owner, "id", 1L);

            Shop shop = TestUtil.createShop(owner);
            ReflectionTestUtils.setField(shop, "id", SHOP_ID);

            Reservation r1 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(2));
            Reservation r3 = TestUtil.createReservation(shop, owner, null,
                LocalDateTime.now().plusHours(3));
            ReflectionTestUtils.setField(r1, "id", 1L);
            ReflectionTestUtils.setField(r2, "id", 2L);
            ReflectionTestUtils.setField(r3, "id", 3L);

            Pageable pageable = PageRequest.of(0, 2, Sort.by(Direction.DESC, "id"));
            Slice<Reservation> slice = new SliceImpl<>(List.of(r3, r2), pageable, true); // 최신순

            // mocking
            given(shopService.shopFind(SHOP_ID)).willReturn(shop);
            given(reservationFacade.findByShopIdWithFilterAndCursor(SHOP_ID, null, null,
                pageable))
                .willReturn(slice);

            // when
            ReservationListResponse result = reservationFacade.getReservations(
                SHOP_ID, null, owner.getId(), null, 2
            );

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content()).extracting("reservationId")
                .containsExactly(3L, 2L);
            assertThat(result.nextCursor()).isEqualTo(2L);
        }

        @Test
        @DisplayName("shopId 없이 소유한 모든 가게의 예약을 조회")
        void shopId_없이_모든_예약_조회() {
            // given
            User owner = TestUtil.createUser();
            ReflectionTestUtils.setField(owner, "id", 1L);

            Shop shop1 = TestUtil.createShop(owner);
            Shop shop2 = TestUtil.createShop(owner);
            ReflectionTestUtils.setField(shop1, "id", 1L);
            ReflectionTestUtils.setField(shop2, "id", 2L);

            Reservation r1 = TestUtil.createReservation(shop1, owner, null, LocalDateTime.now().plusHours(1));
            Reservation r2 = TestUtil.createReservation(shop2, owner, null, LocalDateTime.now().plusHours(2));
            ReflectionTestUtils.setField(r1, "id", 1L);
            ReflectionTestUtils.setField(r2, "id", 2L);

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.DESC, "id"));
            Slice<Reservation> slice = new SliceImpl<>(List.of(r2, r1), pageable, false);

            // mocking
            given(shopService.findByOwnerId(owner.getId())).willReturn(List.of(shop1, shop2));
            given(reservationFacade.findByShopIdsWithFilterAndCursor(
                List.of(1L, 2L), null, null, pageable)).willReturn(slice);

            // when
            ReservationListResponse result = reservationFacade.getReservations(
                null, null, owner.getId(), null, 10
            );

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content()).extracting("reservationId").containsExactly(2L, 1L);
            assertThat(result.nextCursor()).isNull(); // hasNext=false였기 때문에
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

                given(reservationFacade.getReservationById(1L)).willReturn(reservation);

                // when
                reservationFacade.confirmReservation(reservation.getId(),
                    user.getId());

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

                given(reservationFacade.getReservationById(2L)).willReturn(reservation);

                // when
                reservationFacade.refuseReservation(reservation.getId(),
                    user.getId());

                // then
                assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REFUSED);
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

                given(reservationFacade.getReservationById(3L)).willReturn(reservation);

                // when & then
                assertThrows(BusinessException.class, () ->
                    reservationFacade.confirmReservation(reservation.getId(),
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
                reservation.changeStatus(ReservationStatus.REFUSED); // 이미 거절됨

                given(reservationFacade.getReservationById(4L)).willReturn(reservation);

                // when & then
                assertThrows(BusinessException.class, () ->
                    reservationFacade.refuseReservation(reservation.getId(),
                        user.getId())
                );
            }
        }

    }
}
