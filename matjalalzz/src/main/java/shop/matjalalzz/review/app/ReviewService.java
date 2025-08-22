package shop.matjalalzz.review.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.review.dao.ReviewRepository;
import shop.matjalalzz.review.dto.MyReviewPageResponse;
import shop.matjalalzz.review.dto.MyReviewResponse;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.dto.projection.ReviewProjection;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.review.mapper.ReviewMapper;
import shop.matjalalzz.shop.app.ShopService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ReservationService reservationService;
    private final PartyService partyService;
    private final ShopService shopService;
    private final PreSignedProvider preSignedProvider;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = getReview(reviewId);
        validatePermission(review, userId);
        review.delete();
        List<String> imageKeys = review.getImages().stream().map(Image::getS3Key).toList();
        preSignedProvider.deleteObjects(imageKeys);
        removeShopRating(review.getShop(), review.getRating());
    }

    @Transactional
    public PreSignedUrlListResponse createReview(ReviewCreateRequest request, Long writerId) {
        if (reviewRepository.existsByReservationIdAndWriterId(request.reservationId(), writerId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_DATA);
        }

        User writer = userService.getUserById(writerId);
        Reservation reservation = reservationService.getReservationById(request.reservationId());

        if (reservation.getStatus() != ReservationStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        validateReservationPermission(reservation, writerId);

        Shop shop = shopService.shopFind(request.shopId());

        addShopRating(shop, request.rating());

        Review review = ReviewMapper.fromReviewCreateRequest(request, writer, shop, reservation);
        Review result = reviewRepository.save(review);
        return preSignedProvider.createReviewUploadUrls(request.imageCount(), shop.getId(),
            result.getId());
    }

    @Transactional(readOnly = true)
    public ReviewPageResponse findReviewPageByShop(Long shopId, Long cursor, int size) {
        Slice<ReviewProjection> reviews = reviewRepository.findByShopIdAndCursor(shopId, cursor,
            PageRequest.of(0, size));
        Long nextCursor = null;
        if (reviews.hasNext()) {
            nextCursor = reviews.getContent().getLast().getReviewId();
        }
        return ReviewMapper.toReviewPageResponseFromProjection(nextCursor, reviews.getContent(),
            BASE_URL);
    }

    @Transactional(readOnly = true)
    public MyReviewPageResponse findMyReviewPage(Long userId, Long cursor, int size) {
        Slice<MyReviewResponse> comments = reviewRepository.findByUserIdAndCursor(userId, cursor,
            PageRequest.of(0, size));

        Long nextCursor = null;
        if (comments.hasNext()) {
            nextCursor = comments.getContent().getLast().reviewId();
        }

        return ReviewMapper.toMyReviewPageResponse(nextCursor, comments);
    }

    @Transactional(readOnly = true)
    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(
            () -> new BusinessException(ErrorCode.DATA_NOT_FOUND));

    }

    private void validatePermission(Review review, Long actorId) {
        if (!review.getWriter().getId().equals(actorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private void validateReservationPermission(Reservation reservation, Long actorId) {
        if (reservation.getParty() != null) {
            // Party 프록시를 초기화시키지 않고 PartyUsers조회 -> 쿼리 1개 감소
            List<PartyUser> partyUsers = partyService.getPartyUsers(reservation.getParty().getId());
            List<Long> partyUserIds = partyUsers.stream().map(pu ->
                pu.getUser().getId()).toList();
            if (!partyUserIds.contains(actorId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            }
        } else if (!reservation.getUser().getId().equals(actorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private void addShopRating(Shop shop, Double rating) {
        Double currentRating = shop.getRating();
        int currentCount = reviewRepository.countReviewByShop(shop);
        double newRating = currentRating * currentCount + rating;
        newRating /= (currentCount + 1);
        shop.updateRating(newRating);
    }

    private void removeShopRating(Shop shop, Double rating) {
        Double currentRating = shop.getRating();
        int currentCount = reviewRepository.countReviewByShop(shop);
        double newRating = currentRating * currentCount - rating;
        newRating /= (currentCount - 1);
        if (newRating < 0) {
            newRating = 0.0;
        }
        shop.updateRating(newRating);
    }
}
