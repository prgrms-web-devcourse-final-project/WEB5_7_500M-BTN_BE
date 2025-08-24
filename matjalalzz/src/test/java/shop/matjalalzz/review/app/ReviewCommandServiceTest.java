package shop.matjalalzz.review.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@ExtendWith(MockitoExtension.class)
class ReviewCommandServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserService userService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private ShopService shopService;

    @Mock
    private PreSignedProvider preSignedProvider;

    @InjectMocks
    private ReviewCommandService reviewCommandService;

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
                .imageCount(1)
                .build();

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Shop shop = mock(Shop.class);
            when(shop.getId()).thenReturn(shopId);

            Reservation reservation = mock(Reservation.class);
            when(reservation.getUser()).thenReturn(writer);
            when(reservation.getStatus()).thenReturn(ReservationStatus.TERMINATED);

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
            when(preSignedProvider.createReviewUploadUrls(anyInt(), anyLong(),
                anyLong())).thenReturn(
                new PreSignedUrlListResponse(List.of(new PreSignedUrlResponse("key", "url")),
                    shopId));

            // when
            PreSignedUrlListResponse response = reviewCommandService.createReview(request,
                writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.refId()).isEqualTo(reviewId);

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
                .build();

            when(userService.getUserById(writerId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewCommandService.createReview(request, writerId))
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
                .build();

            User writer = mock(User.class);

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewCommandService.createReview(request, writerId))
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
                .build();

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Reservation reservation = mock(Reservation.class);
            when(reservation.getUser()).thenReturn(writer);
            when(reservation.getStatus()).thenReturn(ReservationStatus.TERMINATED);

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);
            when(shopService.shopFind(shopId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewCommandService.createReview(request, writerId))
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
                .build();

            User writer = mock(User.class);

            User reservationUser = mock(User.class);
            when(reservationUser.getId()).thenReturn(reservationUserId);

            Reservation reservation = mock(Reservation.class);
            when(reservation.getUser()).thenReturn(reservationUser);
            when(reservation.getParty()).thenReturn(null); // 파티 예약이 아닌 경우
            when(reservation.getStatus()).thenReturn(ReservationStatus.TERMINATED);

            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(
                reservation);

            // when & then
            assertThatThrownBy(() -> reviewCommandService.createReview(request, writerId))
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
                .build();

            // 이미 동일한 예약과 작성자로 리뷰가 존재한다고 설정
            when(reviewRepository.existsByReservationIdAndWriterId(reservationId, writerId))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewCommandService.createReview(request, writerId))
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

            Image image1 = mock(Image.class);
            when(image1.getS3Key()).thenReturn("key1");

            Shop shop1 = mock(Shop.class);
            when(shop1.getRating()).thenReturn(4.2);

            Review review = Review.builder()
                .id(reviewId)
                .content("맛있어요!")
                .rating(4.5)
                .shop(shop1)
                .writer(writer)
                .images(List.of(image1))
                .build();

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            // when
            reviewCommandService.deleteReview(reviewId, writerId);

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
            assertThatThrownBy(() -> reviewCommandService.deleteReview(reviewId, writerId))
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
            assertThatThrownBy(
                () -> reviewCommandService.deleteReview(reviewId, unauthorizedUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
