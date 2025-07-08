package shop.matjalalzz.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.domain.comment.entity.Comment;
import shop.matjalalzz.domain.mock.MockReservation;
import shop.matjalalzz.domain.mock.MockShop;
import shop.matjalalzz.domain.mock.MockUser;
import shop.matjalalzz.domain.review.dto.ReviewCreateRequest;
import shop.matjalalzz.domain.review.dto.ReviewPageResponse;
import shop.matjalalzz.domain.review.dto.ReviewResponse;
import shop.matjalalzz.domain.review.entity.Review;
import shop.matjalalzz.domain.review.mapper.ReviewMapper;
import shop.matjalalzz.domain.review.repository.ReviewRepository;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Transactional
    public void deleteReview(Long reviewId,Long userId){
        Review review = getReview(reviewId);
        validateReview(review, userId);
        reviewRepository.delete(review);
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, Long userId){
        MockUser writer = MockUser.builder().id(userId).build();
        MockReservation reservation = MockReservation.builder().id(request.reservationId()).build();
        MockShop shop = MockShop.builder().id(1L).build(); // TODO: 식당 조회후 넣어주는 작업
        Review review = ReviewMapper.fromReviewCreateRequest(request,writer,shop,reservation);
        reviewRepository.save(review);
        return ReviewMapper.toReviewResponse(review);
    }

    @Transactional(readOnly = true)
    public ReviewPageResponse findReviewPageByShop(Long shopId, Long cursor, int size) {
        Page<Review> comments = reviewRepository.findByShopIdAndCursor(shopId,cursor, PageRequest.of(0,size));
        Long nextCursor = null;
        if(comments.hasNext()){
            nextCursor = comments.getContent().getLast().getId();
        }
        return ReviewPageResponse.builder()
            .nextCursor(nextCursor)
            .reviews(comments.stream().map(ReviewMapper::toReviewResponse).toList())
            .build();

    }

    private Review getReview(Long reviewId){
        return reviewRepository.findById(reviewId).orElseThrow(
            () -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

    }

    private void validateReview(Review review, Long actorId){
        if(!review.getWriter().getId().equals(actorId)){
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
