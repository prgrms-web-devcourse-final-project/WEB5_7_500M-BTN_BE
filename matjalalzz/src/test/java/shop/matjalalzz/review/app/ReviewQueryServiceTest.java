package shop.matjalalzz.review.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.user.entity.User;

@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewQueryService reviewQueryService;

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

            Image image1 = mock(Image.class);
            when(image1.getS3Key()).thenReturn("key1");

            User writer1 = mock(User.class);
            when(writer1.getNickname()).thenReturn("테스터1");

            User writer2 = mock(User.class);
            when(writer2.getNickname()).thenReturn("테스터2");

            Review review1 = Review.builder()
                .id(1L)
                .content("맛있어요!")
                .rating(4.5)
                .writer(writer1)
                .images(List.of(image1))
                .build();

            Review review2 = Review.builder()
                .id(2L)
                .content("서비스가 좋아요!")
                .rating(5.0)
                .writer(writer2)
                .images(List.of(image1))
                .build();

            List<Review> reviews = List.of(review1, review2);
            Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, size),
                reviews.size());

            when(reviewRepository.findByShopIdAndCursor(shopId, cursor, PageRequest.of(0, size)))
                .thenAnswer(invocation -> reviewPage);

            // when
            ReviewPageResponse response = reviewQueryService.findReviewPageByShop(shopId, cursor,
                size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
            assertThat(response.content().getFirst().reviewId()).isEqualTo(1L);
            assertThat(response.content().getFirst().content()).isEqualTo("맛있어요!");
            assertThat(response.content().get(0).rating()).isEqualTo(4.5);
            assertThat(response.content().get(0).userNickname()).isEqualTo("테스터1");

            assertThat(response.content().get(1).reviewId()).isEqualTo(2L);
            assertThat(response.content().get(1).content()).isEqualTo("서비스가 좋아요!");
            assertThat(response.content().get(1).rating()).isEqualTo(5.0);
            assertThat(response.content().get(1).userNickname()).isEqualTo("테스터2");

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

            Image image1 = mock(Image.class);
            when(image1.getS3Key()).thenReturn("key1");

            User writer1 = mock(User.class);
            when(writer1.getNickname()).thenReturn("테스터1");

            User writer2 = mock(User.class);
            when(writer2.getNickname()).thenReturn("테스터2");

            Review review1 = Review.builder()
                .id(1L)
                .content("맛있어요!")
                .rating(4.5)
                .writer(writer1)
                .images(List.of(image1))
                .build();

            Review review2 = Review.builder()
                .id(2L)
                .content("서비스가 좋아요!")
                .rating(5.0)
                .writer(writer2)
                .images(List.of(image1))
                .build();

            List<Review> reviews = List.of(review1, review2);
            Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, size),
                5);  // 총 5개 중 2개

            when(reviewRepository.findByShopIdAndCursor(shopId, cursor, PageRequest.of(0, size)))
                .thenAnswer(invocation -> reviewPage);

            // when
            ReviewPageResponse response = reviewQueryService.findReviewPageByShop(shopId, cursor,
                size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(2);
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
                .thenAnswer(invocation -> emptyPage);

            // when
            ReviewPageResponse response = reviewQueryService.findReviewPageByShop(shopId, cursor,
                size);

            // then
            assertThat(response).isNotNull();
            assertThat(response.content()).isEmpty();
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
            Review foundReview = reviewQueryService.getReview(reviewId);

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
            assertThatThrownBy(() -> reviewQueryService.getReview(reviewId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND);
        }
    }
}