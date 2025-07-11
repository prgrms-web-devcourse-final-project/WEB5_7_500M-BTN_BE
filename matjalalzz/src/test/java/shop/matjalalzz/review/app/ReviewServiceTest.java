package shop.matjalalzz.review.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewResponse;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.dao.ShopRepository;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ShopRepository shopRepository;

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
            when(writer.getNickname()).thenReturn("테스터");

            Shop shop = mock(Shop.class);

            Reservation reservation = mock(Reservation.class);

            Review review = Review.builder()
                .id(reviewId)
                .content(request.content())
                .rating(request.rating())
                .writer(writer)
                .shop(shop)
                .reservation(reservation)
                .build();

            when(userRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));
            when(reservationRepository.findById(reservationId)).thenReturn(
                Optional.of(reservation));
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

            when(userRepository.findById(writerId)).thenReturn(Optional.empty());

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

            when(userRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

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

            Reservation reservation = mock(Reservation.class);

            when(userRepository.findById(writerId)).thenReturn(Optional.of(writer));
            when(reservationRepository.findById(reservationId)).thenReturn(
                Optional.of(reservation));
            when(shopRepository.findById(shopId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }
    }
}