package shop.matjalalzz.review.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.review.app.ReviewCommandService;
import shop.matjalalzz.review.app.ReviewQueryService;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;

@RestController
@RequiredArgsConstructor
public class ReviewController implements ReviewControllerSpec {

    private final ReviewQueryService reviewQueryService;
    private final ReviewCommandService reviewCommandService;

    @Override
    public BaseResponse<ReviewPageResponse> getReviews(@PathVariable Long shopId,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10", required = false) int size) {
        return BaseResponse.ok(reviewQueryService.findReviewPageByShop(shopId, cursor, size),
            BaseStatus.OK);
    }

    @Override
    public BaseResponse<PreSignedUrlListResponse> createReview(
        @Valid @RequestBody ReviewCreateRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {

        return BaseResponse.ok(reviewCommandService.createReview(request, principal.getId()),
            BaseStatus.CREATED);
    }

    @Override
    public void deleteReview(@PathVariable Long reviewId,
        @AuthenticationPrincipal PrincipalUser principal) {
        reviewCommandService.deleteReview(reviewId, principal.getId());
    }
}
