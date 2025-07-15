package shop.matjalalzz.review.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.dto.ReviewResponse;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserService userService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private ShopService shopService;

    @InjectMocks
    private ReviewService reviewService;

    @Nested
    @DisplayName("리뷰 생성 테스트")
    class CreateReviewTest {

        @Test
        @DisplayName("리뷰 생성 성공")
        void createReview_success() {
            // given
            Long writerId = 1L;
            Long reviewId = 1L;
            Long shopId = 1L;
            Long reservationId = 1L;
            Double rating = 4.5;

            ReviewCreateRequest request = ReviewCreateRequest.builder()
                .shopId(shopId)
                .reservationId(reservationId)
                .content("맛있어요!")
                .rating(rating)
                .images(new ArrayList<>())
                .build();

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);
            when(writer.getNickname()).thenReturn("테스터");

            Shop shop = mock(Shop.class);

            Reservation reservation = mock(Reservation.class);
            when(reservation.getUser()).thenReturn(writer);

            Review review = Review.builder()
                .id(reviewId)
                .content(request.content())
                .rating(request.rating())
                .writer(writer)
                .shop(shop)
                .reservation(reservation)
                .build();

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(shopService.shopFind(shopId)).thenReturn(shop);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);
            when(reviewRepository.save(any(Review.class))).thenReturn(review);

            // when
            ReviewResponse response = reviewService.createReview(request, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.reviewId()).isEqualTo(reviewId);
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.rating()).isEqualTo(rating);
            assertThat(response.userNickname()).isEqualTo("테스터");

            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자가 리뷰 생성 실패")
        void createReview_userNotFound_fail() {
            // given
            Long writerId = 1L;
            Long shopId = 1L;
            Long reservationId = 1L;
            Double rating = 4.5;

            ReviewCreateRequest request = ReviewCreateRequest.builder()
                .shopId(shopId)
                .reservationId(reservationId)
                .content("맛있어요!")
                .rating(rating)
                .images(new ArrayList<>())
                .build();

            when(userService.getUserById(writerId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 예약으로 리뷰 생성 실패")
        void createReview_reservationNotFound_fail() {
            // given
            Long writerId = 1L;
            Long shopId = 1L;
            Long reservationId = 1L;
            Double rating = 4.5;

            ReviewCreateRequest request = ReviewCreateRequest.builder()
                .shopId(shopId)
                .reservationId(reservationId)
                .content("맛있어요!")
                .rating(rating)
                .images(new ArrayList<>())
                .build();

            User writer = mock(User.class);

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 가게로 리뷰 생성 실패")
        void createReview_shopNotFound_fail() {
            // given
            Long writerId = 1L;
            Long shopId = 1L;
            Long reservationId = 1L;
            Double rating = 4.5;

            ReviewCreateRequest request = ReviewCreateRequest.builder()
                .shopId(shopId)
                .reservationId(reservationId)
                .content("맛있어요!")
                .rating(rating)
                .images(new ArrayList<>())
                .build();

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Reservation reservation = mock(Reservation.class);
            when(reservation.getUser()).thenReturn(writer);

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);
            when(shopService.shopFind(shopId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }

        @Test
        @DisplayName("예약 주인이 아닌 사용자가 리뷰 생성 실패")
        void createReview_notReservationOwner_fail() {
            // given
            Long writerId = 1L;
            Long reservationUserId = 2L; // 실제 예약자 ID
            Long shopId = 1L;
            Long reservationId = 1L;
            Double rating = 4.5;

            ReviewCreateRequest request = ReviewCreateRequest.builder()
                .shopId(shopId)
                .reservationId(reservationId)
                .content("맛있어요!")
                .rating(rating)
                .images(new ArrayList<>())
                .build();

            User writer = mock(User.class);

            User reservationUser = mock(User.class);
            when(reservationUser.getId()).thenReturn(reservationUserId);

            Reservation reservation = mock(Reservation.class);
            when(reservation.getUser()).thenReturn(reservationUser);
            when(reservation.getParty()).thenReturn(null); // 파티 예약이 아닌 경우

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(
                reservation);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
        }

        @Test
        @DisplayName("중복 리뷰 생성 실패")
        void createReview_duplicateReview_fail() {
            // given
            Long writerId = 1L;
            Long shopId = 1L;
            Long reservationId = 1L;
            Double rating = 4.5;

            ReviewCreateRequest request = ReviewCreateRequest.builder()
                .shopId(shopId)
                .reservationId(reservationId)
                .content("맛있어요!")
                .rating(rating)
                .images(new ArrayList<>())
                .build();

            // 이미 동일한 예약과 작성자로 리뷰가 존재한다고 설정
            when(reviewRepository.existsByReservationIdAndWriterId(reservationId, writerId))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_DATA);
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 테스트")
    class DeleteReviewTest {

        @Test
        @DisplayName("리뷰 삭제 성공")
        void deleteReview_success() {
            // given
            Long reviewId = 1L;
            Long writerId = 1L;

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Review review = Review.builder()
                .id(reviewId)
                .content("맛있어요!")
                .rating(4.5)
                .writer(writer)
                .build();

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            // when
            reviewService.deleteReview(reviewId, writerId);

            // then
            verify(reviewRepository).findById(reviewId);
            // 삭제 여부 직접 검증은 어렵지만 메서드가 호출되었는지 확인
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 삭제 실패")
        void deleteReview_notFound_fail() {
            // given
            Long reviewId = 1L;
            Long writerId = 1L;

            when(reviewRepository.findById(reviewId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 리뷰 삭제 시도 실패")
        void deleteReview_notWriter_fail() {
            // given
            Long reviewId = 1L;
            Long writerId = 1L;
            Long unauthorizedUserId = 2L;

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Review review = Review.builder()
                .id(reviewId)
                .content("맛있어요!")
                .rating(4.5)
                .writer(writer)
                .build();

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, unauthorizedUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    @Nested
    @DisplayName("리뷰 조회 테스트")
    class FindReviewTest {

        @Test
        @DisplayName("가게별 리뷰 목록 조회 성공")
        void findReviewPageByShop_success() {
            // given
            Long shopId = 1L;
            Long cursor = 0L;
            int size = 10;

            User writer1 = mock(User.class);
            when(writer1.getNickname()).thenReturn("테스터1");

            User writer2 = mock(User.class);
            when(writer2.getNickname()).thenReturn("테스터2");

            Review review1 = Review.builder()
                .id(1L)
                .content("맛있어요!")
                .rating(4.5)
                .writer(writer1)
                .build();

            Review review2 = Review.builder()
                .id(2L)
                .content("서비스가 좋아요!")
                .rating(5.0)
                .writer(writer2)
                .build();

            List<Review> reviews = List.of(review1, review2);
            Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, size),
                reviews.size());

            when(reviewRepository.findByShopIdAndCursor(shopId, cursor, PageRequest.of(0, size)))
                .thenReturn(reviewPage);

            // when
            ReviewPageResponse response = reviewService.findReviewPageByShop(shopId, cursor, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.reviews()).hasSize(2);
            assertThat(response.reviews().getFirst().reviewId()).isEqualTo(1L);
            assertThat(response.reviews().getFirst().content()).isEqualTo("맛있어요!");
            assertThat(response.reviews().get(0).rating()).isEqualTo(4.5);
            assertThat(response.reviews().get(0).userNickname()).isEqualTo("테스터1");

            assertThat(response.reviews().get(1).reviewId()).isEqualTo(2L);
            assertThat(response.reviews().get(1).content()).isEqualTo("서비스가 좋아요!");
            assertThat(response.reviews().get(1).rating()).isEqualTo(5.0);
            assertThat(response.reviews().get(1).userNickname()).isEqualTo("테스터2");

            // 다음 페이지가 없으므로 nextCursor는 null
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("가게별 리뷰 목록 조회 - 다음 페이지 있음")
        void findReviewPageByShop_hasNextPage() {
            // given
            Long shopId = 1L;
            Long cursor = 0L;
            int size = 2;

            User writer1 = mock(User.class);
            when(writer1.getNickname()).thenReturn("테스터1");

            User writer2 = mock(User.class);
            when(writer2.getNickname()).thenReturn("테스터2");

            Review review1 = Review.builder()
                .id(1L)
                .content("맛있어요!")
                .rating(4.5)
                .writer(writer1)
                .build();

            Review review2 = Review.builder()
                .id(2L)
                .content("서비스가 좋아요!")
                .rating(5.0)
                .writer(writer2)
                .build();

            List<Review> reviews = List.of(review1, review2);
            Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, size),
                5);  // 총 5개 중 2개

            when(reviewRepository.findByShopIdAndCursor(shopId, cursor, PageRequest.of(0, size)))
                .thenReturn(reviewPage);

            // when
            ReviewPageResponse response = reviewService.findReviewPageByShop(shopId, cursor, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.reviews()).hasSize(2);
            // 다음 페이지가 있으므로 nextCursor는 마지막 리뷰의 ID
            assertThat(response.nextCursor()).isEqualTo(2L);
        }

        @Test
        @DisplayName("가게별 리뷰가 없는 경우 빈 목록 반환")
        void findReviewPageByShop_emptyList() {
            // given
            Long shopId = 1L;
            Long cursor = 0L;
            int size = 10;

            Page<Review> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, size), 0);
            when(reviewRepository.findByShopIdAndCursor(shopId, cursor, PageRequest.of(0, size)))
                .thenReturn(emptyPage);

            // when
            ReviewPageResponse response = reviewService.findReviewPageByShop(shopId, cursor, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.reviews()).isEmpty();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("단일 리뷰 조회 성공")
        void getReview_success() {
            // given
            Long reviewId = 1L;

            User writer = mock(User.class);

            Review review = Review.builder()
                .id(reviewId)
                .content("맛있어요!")
                .rating(4.5)
                .writer(writer)
                .build();

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            // when
            Review foundReview = reviewService.getReview(reviewId);

            // then
            assertThat(foundReview).isNotNull();
            assertThat(foundReview.getId()).isEqualTo(reviewId);
            assertThat(foundReview.getContent()).isEqualTo("맛있어요!");
            assertThat(foundReview.getRating()).isEqualTo(4.5);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 조회 실패")
        void getReview_notFound_fail() {
            // given
            Long reviewId = 1L;

            when(reviewRepository.findById(reviewId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewService.getReview(reviewId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }
    }
}