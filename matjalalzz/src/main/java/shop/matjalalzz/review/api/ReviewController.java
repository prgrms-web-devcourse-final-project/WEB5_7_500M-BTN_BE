package shop.matjalalzz.review.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.review.app.ReviewService;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "리뷰 API", description = "댓글 리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 조회", description = "특정 가게의 리뷰 목록을 조회합니다.(Completed)")
    @GetMapping("/shops/{shopId}/reviews")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ReviewPageResponse> getReviews(@PathVariable Long shopId,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10", required = false) int size) {
        return BaseResponse.ok(reviewService.findReviewPageByShop(shopId, cursor, size),
            BaseStatus.OK);
    }

    @Operation(summary = "리뷰 작성", description = "리뷰를 작성합니다.(Completed)")
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<PreSignedUrlListResponse> createReview(
        @Valid @RequestBody ReviewCreateRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {

        return BaseResponse.ok(reviewService.createReview(request, principal.getId()),
            BaseStatus.CREATED);
    }

    @Operation(summary = "리뷰 삭제", description = "특정 리뷰를 삭제합니다.(Completed)")
    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long reviewId,
        @AuthenticationPrincipal PrincipalUser principal) {
        reviewService.deleteReview(reviewId, principal.getId());
    }
}
