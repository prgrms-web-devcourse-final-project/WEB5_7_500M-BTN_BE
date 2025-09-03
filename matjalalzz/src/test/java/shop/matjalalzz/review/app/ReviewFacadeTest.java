package shop.matjalalzz.review.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.review.dto.MyReviewPageResponse;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.dto.projection.ReviewProjection;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.shop.app.ShopFacade;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@ExtendWith(MockitoExtension.class)
class ReviewFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private PartyService partyService;

    @Mock
    private ShopFacade shopFacade;

    @Mock
    private PreSignedProvider preSignedProvider;

    @Mock
    private ReviewQueryService reviewQueryService;

    @Mock
    private ReviewCommandService reviewCommandService;

    @InjectMocks
    private ReviewFacade reviewFacade;

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
            when(reservation.getParty()).thenReturn(null);

            Review review = Review.builder()
                .id(reviewId)
                .content(request.content())
                .rating(request.rating())
                .writer(writer)
                .shop(shop)
                .reservation(reservation)
                .build();

            doNothing().when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);
            when(shopFacade.findShop(shopId)).thenReturn(shop);
            doNothing().when(reviewCommandService).addShopRating(shop, rating);
            when(reviewCommandService.save(any(Review.class))).thenReturn(review);
            when(preSignedProvider.createReviewUploadUrls(anyInt(), anyLong(),
                anyLong())).thenReturn(
                new PreSignedUrlListResponse(List.of(new PreSignedUrlResponse("key", "url")),
                    reviewId));

            // when
            PreSignedUrlListResponse response = reviewFacade.createReview(request, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.refId()).isEqualTo(reviewId);

            verify(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            verify(reviewCommandService).addShopRating(shop, rating);
            verify(reviewCommandService).save(any(Review.class));
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

            doThrow(new BusinessException(ErrorCode.DUPLICATE_DATA))
                .when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_DATA);
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

            doNothing().when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            when(userService.getUserById(writerId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(request, writerId))
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

            doNothing().when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(request, writerId))
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
            when(reservation.getParty()).thenReturn(null);

            doNothing().when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);
            when(shopFacade.findShop(shopId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }

        @Test
        @DisplayName("예약 주인이 아닌 사용자가 리뷰 생성 실패")
        void createReview_notReservationOwner_fail() {
            // given
            Long writerId = 1L;
            Long reservationUserId = 2L;
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
            when(reservation.getParty()).thenReturn(null);
            when(reservation.getStatus()).thenReturn(ReservationStatus.TERMINATED);

            doNothing().when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
        }

        @Test
        @DisplayName("완료되지 않은 예약으로 리뷰 생성 실패")
        void createReview_invalidReservationStatus_fail() {
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
            when(reservation.getStatus()).thenReturn(ReservationStatus.CONFIRMED); // TERMINATED가 아님
            when(reservation.getParty()).thenReturn(null);

            doNothing().when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_STATUS);
        }

        @Test
        @DisplayName("파티 예약에서 파티원이 아닌 사용자가 리뷰 생성 실패")
        void createReview_notPartyMember_fail() {
            // given
            Long writerId = 1L;
            Long shopId = 1L;
            Long reservationId = 1L;
            Long partyId = 1L;
            Double rating = 4.5;

            ReviewCreateRequest request = ReviewCreateRequest.builder()
                .shopId(shopId)
                .reservationId(reservationId)
                .content("맛있어요!")
                .rating(rating)
                .build();

            User writer = mock(User.class);
            when(writer.getId()).thenReturn(writerId);

            Party party = mock(Party.class);
            when(party.getId()).thenReturn(partyId);

            User partyLeader = mock(User.class);
            when(partyLeader.getId()).thenReturn(2L);

            Reservation reservation = mock(Reservation.class);
            when(reservation.getUser()).thenReturn(partyLeader);
            when(reservation.getParty()).thenReturn(party);
            when(reservation.getStatus()).thenReturn(ReservationStatus.TERMINATED);

            PartyUser partyUser1 = mock(PartyUser.class);
            User member1 = mock(User.class);
            when(member1.getId()).thenReturn(3L);
            when(partyUser1.getUser()).thenReturn(member1);

            PartyUser partyUser2 = mock(PartyUser.class);
            User member2 = mock(User.class);
            when(member2.getId()).thenReturn(4L);
            when(partyUser2.getUser()).thenReturn(member2);

            List<PartyUser> partyUsers = List.of(partyUser1, partyUser2);

            doNothing().when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);
            when(partyService.getPartyUsers(partyId)).thenReturn(partyUsers);

            // when & then
            assertThatThrownBy(() -> reviewFacade.createReview(request, writerId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
        }

        @Test
        @DisplayName("파티 예약에서 파티원이 리뷰 생성 성공")
        void createReview_partyMember_success() {
            // given
            Long writerId = 1L;
            Long reviewId = 1L;
            Long shopId = 1L;
            Long reservationId = 1L;
            Long partyId = 1L;
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

            Party party = mock(Party.class);
            when(party.getId()).thenReturn(partyId);

            User partyLeader = mock(User.class);
            when(partyLeader.getId()).thenReturn(2L);

            Reservation reservation = mock(Reservation.class);
            when(reservation.getUser()).thenReturn(partyLeader);
            when(reservation.getParty()).thenReturn(party);
            when(reservation.getStatus()).thenReturn(ReservationStatus.TERMINATED);

            PartyUser partyUser1 = mock(PartyUser.class);
            when(partyUser1.getUser()).thenReturn(writer);

            PartyUser partyUser2 = mock(PartyUser.class);
            User member2 = mock(User.class);
            when(member2.getId()).thenReturn(3L);
            when(partyUser2.getUser()).thenReturn(member2);

            List<PartyUser> partyUsers = List.of(partyUser1, partyUser2);

            Review review = Review.builder()
                .id(reviewId)
                .content(request.content())
                .rating(request.rating())
                .writer(writer)
                .shop(shop)
                .reservation(reservation)
                .build();

            doNothing().when(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            when(userService.getUserById(writerId)).thenReturn(writer);
            when(reservationService.getReservationById(reservationId)).thenReturn(reservation);
            when(partyService.getPartyUsers(partyId)).thenReturn(partyUsers);
            when(shopFacade.findShop(shopId)).thenReturn(shop);
            doNothing().when(reviewCommandService).addShopRating(shop, rating);
            when(reviewCommandService.save(any(Review.class))).thenReturn(review);
            when(preSignedProvider.createReviewUploadUrls(anyInt(), anyLong(),
                anyLong())).thenReturn(
                new PreSignedUrlListResponse(List.of(new PreSignedUrlResponse("key", "url")),
                    reviewId));

            // when
            PreSignedUrlListResponse response = reviewFacade.createReview(request, writerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.refId()).isEqualTo(reviewId);

            verify(partyService).getPartyUsers(partyId);
            verify(reviewQueryService).validateDuplicatedReview(reservationId, writerId);
            verify(reviewCommandService).addShopRating(shop, rating);
            verify(reviewCommandService).save(any(Review.class));
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

            Shop shop = mock(Shop.class);

            Image image1 = mock(Image.class);
            when(image1.getS3Key()).thenReturn("key1");

            Review review = Review.builder()
                .id(reviewId)
                .content("맛있어요!")
                .rating(4.5)
                .writer(writer)
                .shop(shop)
                .images(List.of(image1))
                .build();

            when(reviewQueryService.getReview(reviewId)).thenReturn(review);
            doNothing().when(preSignedProvider).deleteObjects(anyList());
            doNothing().when(reviewCommandService).removeShopRating(shop, 4.5);

            // when
            reviewFacade.deleteReview(reviewId, writerId);

            // then
            verify(reviewQueryService).getReview(reviewId);
            verify(preSignedProvider).deleteObjects(List.of("key1"));
            verify(reviewCommandService).removeShopRating(shop, 4.5);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 삭제 실패")
        void deleteReview_notFound_fail() {
            // given
            Long reviewId = 1L;
            Long writerId = 1L;

            when(reviewQueryService.getReview(reviewId)).thenThrow(
                new BusinessException(ErrorCode.DATA_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewFacade.deleteReview(reviewId, writerId))
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

            when(reviewQueryService.getReview(reviewId)).thenReturn(review);

            // when & then
            assertThatThrownBy(() -> reviewFacade.deleteReview(reviewId, unauthorizedUserId))
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
            String baseUrl = "https://example.com";

            ReflectionTestUtils.setField(reviewFacade, "BASE_URL", baseUrl);

            ReviewProjection projection1 = mock(ReviewProjection.class);
            when(projection1.getReviewId()).thenReturn(1L);
            when(projection1.getContent()).thenReturn("맛있어요!");
            when(projection1.getRating()).thenReturn(4.5);
            when(projection1.getUserNickname()).thenReturn("테스터1");

            ReviewProjection projection2 = mock(ReviewProjection.class);
            when(projection2.getReviewId()).thenReturn(2L);
            when(projection2.getContent()).thenReturn("서비스가 좋아요!");
            when(projection2.getRating()).thenReturn(5.0);
            when(projection2.getUserNickname()).thenReturn("테스터2");

            List<ReviewProjection> projections = List.of(projection1, projection2);
            Slice<ReviewProjection> reviewSlice = new SliceImpl<>(projections,
                PageRequest.of(0, size), false);

            when(reviewQueryService.findReviewPageByShop(shopId, cursor, size)).thenReturn(
                reviewSlice);

            // when
            ReviewPageResponse response = reviewFacade.findReviewPageByShop(shopId, cursor, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.nextCursor()).isNull(); // hasNext() = false이므로
            verify(reviewQueryService).findReviewPageByShop(shopId, cursor, size);
        }

        @Test
        @DisplayName("가게별 리뷰 목록 조회 - 다음 페이지 있음")
        void findReviewPageByShop_hasNextPage() {
            // given
            Long shopId = 1L;
            Long cursor = 0L;
            int size = 2;
            String baseUrl = "https://example.com";

            ReflectionTestUtils.setField(reviewFacade, "BASE_URL", baseUrl);

            ReviewProjection projection1 = mock(ReviewProjection.class);
            when(projection1.getReviewId()).thenReturn(1L);

            ReviewProjection projection2 = mock(ReviewProjection.class);
            when(projection2.getReviewId()).thenReturn(2L);

            List<ReviewProjection> projections = List.of(projection1, projection2);
            Slice<ReviewProjection> reviewSlice = new SliceImpl<>(projections,
                PageRequest.of(0, size), true); // hasNext = true

            when(reviewQueryService.findReviewPageByShop(shopId, cursor, size)).thenReturn(
                reviewSlice);

            // when
            ReviewPageResponse response = reviewFacade.findReviewPageByShop(shopId, cursor, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.nextCursor()).isEqualTo(2L); // 마지막 아이템의 ID
        }

        @Test
        @DisplayName("내 리뷰 목록 조회 성공")
        void findMyReviewPage_success() {
            // given
            Long userId = 1L;
            Long cursor = 0L;
            int size = 10;

            MyReviewResponse review1 = mock(MyReviewResponse.class);
            when(review1.reviewId()).thenReturn(1L);

            MyReviewResponse review2 = mock(MyReviewResponse.class);
            when(review2.reviewId()).thenReturn(2L);

            List<MyReviewResponse> reviews = List.of(review1, review2);
            Slice<MyReviewResponse> reviewSlice = new SliceImpl<>(reviews, PageRequest.of(0, size),
                false);

            when(reviewQueryService.findReviewPageByUser(userId, cursor, size)).thenReturn(
                reviewSlice);

            // when
            MyReviewPageResponse response = reviewFacade.findMyReviewPage(userId, cursor, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.nextCursor()).isNull(); // hasNext() = false이므로
            verify(reviewQueryService).findReviewPageByUser(userId, cursor, size);
        }

        @Test
        @DisplayName("내 리뷰 목록 조회 - 다음 페이지 있음")
        void findMyReviewPage_hasNextPage() {
            // given
            Long userId = 1L;
            Long cursor = 0L;
            int size = 2;

            MyReviewResponse review1 = mock(MyReviewResponse.class);
            when(review1.reviewId()).thenReturn(1L);

            MyReviewResponse review2 = mock(MyReviewResponse.class);
            when(review2.reviewId()).thenReturn(2L);

            List<MyReviewResponse> reviews = List.of(review1, review2);
            Slice<MyReviewResponse> reviewSlice = new SliceImpl<>(reviews, PageRequest.of(0, size),
                true); // hasNext = true

            when(reviewQueryService.findReviewPageByUser(userId, cursor, size)).thenReturn(
                reviewSlice);

            // when
            MyReviewPageResponse response = reviewFacade.findMyReviewPage(userId, cursor, size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.nextCursor()).isEqualTo(2L); // 마지막 아이템의 ID
        }
    }
}
