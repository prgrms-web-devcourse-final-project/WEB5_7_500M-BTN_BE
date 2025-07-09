package shop.matjalalzz.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.user.dto.MyInfoResponse;
import shop.matjalalzz.user.dto.MyInfoUpdateRequest;
import shop.matjalalzz.user.dto.MyPartiesResponse;
import shop.matjalalzz.user.dto.MyReservationsResponse;
import shop.matjalalzz.user.dto.MyReviewsResponse;
import shop.matjalalzz.user.dto.PartyResponse;
import shop.matjalalzz.user.dto.ReservationResponse;
import shop.matjalalzz.user.dto.ReviewResponse;

@Tag(name = "User MyPage", description = "마이페이지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/my-page")
public class UserInfoController {

    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 사용자의 마이페이지 정보를 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
        }
    )
    @GetMapping
    public BaseResponse<MyInfoResponse> getMyInfo() {
        // 이후 구현
        MyInfoResponse data = MyInfoResponse.builder()
            .email("minji97@gmail.com")
            .nickname("맛잘알민지")
            .role("USER")
            .name("김민지")
            .age(28)
            .gender("W")
            .point(1800)
            .phoneNumber("010-1234-5678")
            .bucketId("UUID_a.png")
            .profile("https://s3.amazonaws.com/bucket/uploads/reviews/UUID_a.png")
            .build();

        return BaseResponse.ok(data, HttpStatus.OK);
    }

    @Operation(
        summary = "내 정보 수정",
        description = "로그인한 사용자의 마이페이지 정보를 수정합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "수정 성공")
        }
    )
    @PatchMapping
    public BaseResponse<Void> updateMyInfo(
        @Valid @RequestBody MyInfoUpdateRequest request
    ) {
        // 이후 구현
        return BaseResponse.okOnlyStatus(HttpStatus.OK);
    }

    @Operation(
        summary = "내 예약 정보 조회",
        description = "로그인한 사용자의 예약 목록을 커서 기반 페이징 방식으로 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    @GetMapping("/reservations")
    public BaseResponse<MyReservationsResponse> getMyReservations(
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        // 이후 구현
        MyReservationsResponse data = MyReservationsResponse.builder()
            .nextCursor(100L)
            .content(List.of(
                ReservationResponse.builder()
                    .reservationId(90L)
                    .shopName("신전떡볶이 강남점")
                    .name("이초롱")
                    .reservedAt("2025-07-10T18:00:00")
                    .headCount(2)
                    .reservationFee(4000)
                    .status("CONFIRMED")
                    .build()
            ))
            .build();
        return BaseResponse.ok(data, HttpStatus.OK);
    }

    @Operation(
        summary = "내 파티 정보 조회",
        description = "로그인한 사용자의 파티 목록을 커서 기반 페이징 방식으로 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    @GetMapping("/parties")
    public BaseResponse<MyPartiesResponse> getMyParties(
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        // 이후 구현
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

        return BaseResponse.ok(data, HttpStatus.OK);
    }

    @Operation(
        summary = "내 리뷰 정보 조회",
        description = "로그인한 사용자가 작성한 리뷰 목록을 커서 기반 페이징 방식으로 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
        }
    )
    @GetMapping("/reviews")
    public BaseResponse<MyReviewsResponse> getMyReviews(
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "cursor", required = false) Long cursor
    ) {
        MyReviewsResponse data = MyReviewsResponse.builder()
            .nextCursor(20L)
            .content(List.of(
                ReviewResponse.builder()
                    .reviewId(19L)
                    .shopName("엽기떡볶이 잠실점")
                    .rating(4.5)
                    .content("맵찔이도 먹기 좋았어요!")
                    .createdAt("2025-07-01T15:32:00")
                    .images(List.of(
                        "https://s3.amazonaws.com/bucket/uploads/reviews/UUID_a.png",
                        "https://s3.amazonaws.com/bucket/uploads/reviews/UUID_b.jpg"
                    ))
                    .build()
            ))
            .build();

        return BaseResponse.ok(data, HttpStatus.OK);
    }
}
