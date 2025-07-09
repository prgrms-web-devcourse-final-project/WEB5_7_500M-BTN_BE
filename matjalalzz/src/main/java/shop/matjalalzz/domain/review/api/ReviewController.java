package shop.matjalalzz.domain.review.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import shop.matjalalzz.domain.review.app.ReviewService;
import shop.matjalalzz.domain.review.dto.ReviewCreateRequest;
import shop.matjalalzz.domain.review.dto.ReviewPageResponse;
import shop.matjalalzz.global.unit.BaseResponse;
import shop.matjalalzz.user.adapter.UserDetail;

@RestController
@RequiredArgsConstructor
@Tag(name = "리뷰 API", description = "댓글 리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 조회", description = "특정 가게의 리뷰 목록을 조회합니다.")
    @GetMapping("/shops/{shopId}/reviews")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ReviewPageResponse> getReviews(@PathVariable Long shopId,
        @RequestParam(defaultValue = "0") Long cursor,
        @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.ok(reviewService.findReviewPageByShop(shopId, cursor, size),
            HttpStatus.OK);
    }

    @Operation(summary = "리뷰 작성", description = "리뷰를 작성합니다.")
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<Void> createReview(@RequestBody ReviewCreateRequest request,
        @AuthenticationPrincipal UserDetail userDetail) {
        reviewService.createReview(request, userDetail.getId());
        return BaseResponse.okOnlyStatus(HttpStatus.CREATED);
    }

    @Operation(summary = "리뷰 삭제", description = "특정 리뷰를 삭제합니다.")
    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long reviewId,
        @AuthenticationPrincipal UserDetail userDetail) {
        reviewService.deleteReview(reviewId, userDetail.getId());
    }
}
