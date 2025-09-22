package shop.matjalalzz.review.app;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.image.app.query.ImageQueryService;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.PartyUser;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.entity.Reservation;
import shop.matjalalzz.reservation.entity.ReservationStatus;
import shop.matjalalzz.review.dto.MyReviewPageResponse;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;
import shop.matjalalzz.review.dto.projection.MyReviewProjection;
import shop.matjalalzz.review.dto.projection.ReviewProjection;
import shop.matjalalzz.review.entity.Review;
import shop.matjalalzz.review.mapper.ReviewMapper;
import shop.matjalalzz.shop.app.query.ShopQueryService;
import shop.matjalalzz.shop.entity.Shop;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ReviewFacade {

    private final UserService userService;
    private final ReservationService reservationService;
    private final PartyService partyService;
    private final ShopQueryService shopQueryService;
    private final PreSignedProvider preSignedProvider;
    private final ReviewQueryService reviewQueryService;
    private final ReviewCommandService reviewCommandService;
    private final ImageQueryService imageQueryService;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewQueryService.getReview(reviewId);
        validatePermission(review, userId);
        review.delete();
        List<String> imageKeys = review.getImages().stream().map(Image::getS3Key).toList();
        preSignedProvider.deleteObjects(imageKeys);
        reviewCommandService.removeShopRating(review.getShop(), review.getRating());
    }

    @Transactional
    public PreSignedUrlListResponse createReview(ReviewCreateRequest request, Long writerId) {
        reviewQueryService.validateDuplicatedReview(request.reservationId(), writerId);

        User writer = userService.getUserById(writerId);
        Reservation reservation = reservationService.getReservationById(request.reservationId());

        if (reservation.getStatus() != ReservationStatus.TERMINATED) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        validateReservationPermission(reservation, writerId);

        Shop shop = shopQueryService.findShop(request.shopId());

        reviewCommandService.addShopRating(shop, request.rating());

        Review review = ReviewMapper.fromReviewCreateRequest(request, writer, shop, reservation);
        Review result = reviewCommandService.save(review);
        return preSignedProvider.createReviewUploadUrls(request.imageCount(), shop.getId(),
            result.getId());
    }

    @Transactional(readOnly = true)
    public ReviewPageResponse findReviewPageByShop(Long shopId, Long cursor, int size) {
        Slice<ReviewProjection> reviews = reviewQueryService.findReviewPageByShop(shopId, cursor,
            size);
        Long nextCursor = null;
        if (reviews.hasNext()) {
            nextCursor = reviews.getContent().getLast().getReviewId();
        }
        return ReviewMapper.toReviewPageResponseFromProjection(nextCursor, reviews.getContent(),
            BASE_URL);
    }

    @Transactional(readOnly = true)
    public MyReviewPageResponse findMyReviewPage(Long userId, Long cursor, int size) {
        Slice<MyReviewProjection> reviews = reviewQueryService.findReviewPageByUser(userId, cursor,
            size);

        Long nextCursor = null;
        if (reviews.hasNext()) {
            nextCursor = reviews.getContent().getLast().getReviewId();
        }

        List<Long> reviewIds = reviews.getContent().stream().map(MyReviewProjection::getReviewId)
            .toList();

        Map<Long, List<String>> reviewImages = imageQueryService.findReviewImagesById(
            reviewIds);

        return ReviewMapper.toMyReviewPageResponse(nextCursor, reviews, reviewImages);
    }

    @Transactional(readOnly = true)
    public int findReviewCountByShop(long shopId) {
        return reviewQueryService.findReviewCountByShop(shopId);
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

}
