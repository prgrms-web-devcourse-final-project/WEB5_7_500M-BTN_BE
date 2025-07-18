package shop.matjalalzz.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.dto.MyPartyPageResponse;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.review.app.ReviewService;
import shop.matjalalzz.review.dto.MyReviewPageResponse;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.dto.DeleteProfileRequest;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;

@Tag(name = "마이페이지 API", description = "마이페이지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/my-page")
public class UserInfoController {

    private final UserService userService;
    private final ReservationService reservationService;
    private final PartyService partyService;
    private final ReviewService reviewService;
    private final PreSignedProvider preSignedProvider;

    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 사용자의 마이페이지 정보를 조회합니다.(Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
        }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<MyInfoResponse> getMyInfo(@AuthenticationPrincipal PrincipalUser userInfo) {
        MyInfoResponse result = userService.getMyInfo(userInfo.getId());

        return BaseResponse.ok(result, BaseStatus.OK);
    }

    @Operation(
        summary = "내 정보 수정",
        description = "로그인한 사용자의 마이페이지 정보를 수정합니다.(Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "수정 성공")
        }
    )
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> updateMyInfo(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @Valid @RequestBody MyInfoUpdateRequest request
    ) {
        userService.updateMyInfo(userInfo.getId(), request);

        return BaseResponse.ok(BaseStatus.OK);
    }

    @Operation(
        summary = "프로필 이미지 업로드를 위한 pre-signed url 생성",
        description = "내 정보 수정 항목 중 프로필 이미지를 업로드하기 위한 pre-signed url을 생성합니다.(Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "생성 성공")
        }
    )
    @PostMapping("/presigned-urls")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<PreSignedUrlResponse> getProfilePresignedUrl(
        @AuthenticationPrincipal PrincipalUser userInfo
    ) {
        PreSignedUrlResponse result = preSignedProvider.createProfileUploadUrls(userInfo.getId());

        return BaseResponse.ok(result, BaseStatus.OK);
    }

    @Operation(
        summary = "프로필 이미지 삭제",
        description = "프로필 수정 작업 중 예외 발생으로 업로드된 이미지를 삭제합니다.(Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "삭제 성공")
        }
    )
    @DeleteMapping("/profile-img")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(
        @Valid @RequestBody DeleteProfileRequest request
    ) {
        preSignedProvider.deleteObject(request.profileKey());
    }

    @Operation(
        summary = "내 예약 정보 조회",
        description = "로그인한 사용자의 예약 목록을 커서 기반 페이징 방식으로 조회합니다.(Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    @GetMapping("/reservations")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<MyReservationPageResponse> getMyReservations(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        MyReservationPageResponse result = reservationService.findMyReservationPage(userInfo.getId(), cursor, size);

        return BaseResponse.ok(result, BaseStatus.OK);
    }

    @Operation(
        summary = "내 파티 정보 조회",
        description = "로그인한 사용자의 파티 목록을 커서 기반 페이징 방식으로 조회합니다.(Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    @GetMapping("/parties")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<MyPartyPageResponse> getMyParties(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        MyPartyPageResponse result = partyService.findMyReservationPage(
            userInfo.getId(), cursor, size);

        return BaseResponse.ok(result, BaseStatus.OK);
    }

    @Operation(
        summary = "내 리뷰 정보 조회",
        description = "로그인한 사용자가 작성한 리뷰 목록을 커서 기반 페이징 방식으로 조회합니다.(Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    @GetMapping("/reviews")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<MyReviewPageResponse> getMyReviews(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        MyReviewPageResponse result = reviewService.findMyReviewPage(
            userInfo.getId(), cursor, size);

        return BaseResponse.ok(result, BaseStatus.OK);
    }
}
