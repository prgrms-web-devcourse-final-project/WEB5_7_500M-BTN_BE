package shop.matjalalzz.review.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.review.dto.ReviewCreateRequest;
import shop.matjalalzz.review.dto.ReviewPageResponse;

@Tag(name = "리뷰 API", description = "댓글 리뷰 API")
public interface ReviewControllerSpec {

    @Operation(summary = "리뷰 조회", description = "특정 가게의 리뷰 목록을 조회합니다.(Completed)")
    @GetMapping("/shops/{shopId}/reviews")
    @ResponseStatus(HttpStatus.OK)
    BaseResponse<ReviewPageResponse> getReviews(Long shopId, Long cursor, int size);

    @Operation(summary = "리뷰 작성", description = "리뷰를 작성합니다.(Completed)")
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    BaseResponse<PreSignedUrlListResponse> createReview(ReviewCreateRequest request,
        PrincipalUser principal);

    @Operation(summary = "리뷰 삭제", description = "특정 리뷰를 삭제합니다.(Completed)")
    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteReview(Long reviewId, PrincipalUser principal);
}
