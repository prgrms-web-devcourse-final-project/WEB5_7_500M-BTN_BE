package shop.matjalalzz.review.app;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.reservation.dao.ReservationRepository;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.dto.ReviewResponse;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.review.mapper.ReviewMapper;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.shop.entity.ShopRepository;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = getReview(reviewId);
        validatePermission(review, userId);
        review.delete();
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, Long writerId) {
        User writer = userRepository.findById(writerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND)); //TODO: 개선
        Reservation reservation = reservationRepository.findById(request.reservationId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND)); // TODO: 개선
        Shop shop = shopRepository.findById(request.shopId())
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND)); //TODO: 개선

        Review review = ReviewMapper.fromReviewCreateRequest(request, writer, shop, reservation);
        reviewRepository.save(review);
        return ReviewMapper.toReviewResponse(review);
    }

    @Transactional(readOnly = true)
    public ReviewPageResponse findReviewPageByShop(Long shopId, Long cursor, int size) {
        Page<Review> comments = reviewRepository.findByShopIdAndCursor(shopId, cursor,
            PageRequest.of(0, size));
        Long nextCursor = null;
        if (comments.hasNext()) {
            nextCursor = comments.getContent().getLast().getId();
        }
        return ReviewPageResponse.builder()
            .nextCursor(nextCursor)
            .reviews(comments.stream().map(ReviewMapper::toReviewResponse).toList())
            .build();

    }

    private Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(
            () -> new BusinessException(ErrorCode.DATA_NOT_FOUND));

    }

    private void validatePermission(Review review, Long actorId) {
        if (!review.getWriter().getId().equals(actorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
