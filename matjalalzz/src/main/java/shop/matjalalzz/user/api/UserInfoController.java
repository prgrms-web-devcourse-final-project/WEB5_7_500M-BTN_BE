package shop.matjalalzz.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.dto.MyReservationPageResponse;
import shop.matjalalzz.review.app.ReviewService;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;
import shop.matjalalzz.user.dto.MyPartiesResponse;
import shop.matjalalzz.review.dto.MyReviewPageResponse;
import shop.matjalalzz.user.dto.PartyResponse;

@Tag(name = "User MyPage", description = "마이페이지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/my-page")
public class UserInfoController {

    private final UserService userService;
    private final ReservationService reservationService;
    private final PartyService partyService;
    private final ReviewService reviewService;

    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 사용자의 마이페이지 정보를 조회합니다.(Inprogress)",
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
        description = "로그인한 사용자의 마이페이지 정보를 수정합니다.(Inprogress)",
        responses = {
            @ApiResponse(responseCode = "200", description = "수정 성공")
        }
    )
    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> updateMyInfo(
        @AuthenticationPrincipal PrincipalUser userInfo,
        @Valid @RequestBody MyInfoUpdateRequest request
    ) {
        userService.updateMyInfo(userInfo.getId(), request);

        return BaseResponse.ok(BaseStatus.OK);
    }

    @Operation(
        summary = "내 예약 정보 조회",
        description = "로그인한 사용자의 예약 목록을 커서 기반 페이징 방식으로 조회합니다.(Inprogress)",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    @GetMapping("/reservations")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<MyReservationPageResponse> getMyReservations(
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        MyReservationPageResponse result = reservationService.

        return BaseResponse.ok(result, BaseStatus.OK);
    }

    @Operation(
        summary = "내 파티 정보 조회",
        description = "로그인한 사용자의 파티 목록을 커서 기반 페이징 방식으로 조회합니다.(Inprogress)",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    @GetMapping("/parties")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<MyPartiesResponse> getMyParties(
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        // todo: 이후 구현
        MyPartiesResponse data = MyPartiesResponse.builder()
            .nextCursor(10L)
            .content(List.of(
                PartyResponse.builder()
                    .partyId(9L)
                    .title("신전떡볶이 먹을 사람?")
                    .shopName("신전떡볶이 강남점")
                    .metAt("2025-07-10T18:00:00")
                    .deadline("2025-07-09T18:00:00")
                    .status("COMPLETED")
                    .maxCount(5)
                    .minCount(2)
                    .currentCount(3)
                    .genderCondition("M")
                    .ageCondition(20)
                    .discription("신전떡볶이 강남점은 뭔가 맛이 다르다는데, 가보실 분 구합니다!")
                    .build()
            ))
            .build();

        return BaseResponse.ok(data, BaseStatus.OK);
    }

    @Operation(
        summary = "내 리뷰 정보 조회",
        description = "로그인한 사용자가 작성한 리뷰 목록을 커서 기반 페이징 방식으로 조회합니다.(Inprogress)",
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
