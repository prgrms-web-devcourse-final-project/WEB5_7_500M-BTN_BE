package shop.matjalalzz.reservation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;
import shop.matjalalzz.reservation.entity.ReservationStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "예약 API", description = "예약 관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
        summary = "식당 예약 목록 조회",
        description = "식당의 예약 목록을 필터와 커서 기반으로 조회한다.(Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공",
                content = @Content(schema = @Schema(implementation = ReservationListResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 shopId")
        }
    )
    @GetMapping("/reservations")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ReservationListResponse> getReservations(
        @RequestParam(required = false) ReservationStatus filter,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) Long shopId,
        @AuthenticationPrincipal PrincipalUser userInfo
    ) {

        ReservationListResponse response = reservationService.getReservations(shopId, filter,
            userInfo.getId(), cursor, size);

        return BaseResponse.ok(response, BaseStatus.OK);
    }

    @Operation(
        summary = "예약 생성",
        description = "shopId에 해당하는 식당에 예약을 생성한다. 파티 예약인 경우 partyId를 쿼리 파라미터로 전달해야 한다.(Completed)",
        parameters = {
            @Parameter(
                name = "partyId",
                description = "파티 ID (선택값). 파티 예약일 경우 전달.",
                in = ParameterIn.QUERY,
                required = false,
                example = "3"
            )
        },
        responses = {
            @ApiResponse(responseCode = "201", description = "예약 생성 성공",
                content = @Content(schema = @Schema(implementation = CreateReservationResponse.class)))
        }
    )
    @PostMapping("/shops/{shopId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<CreateReservationResponse> createReservation(
        @PathVariable Long shopId,
        @Valid @RequestBody CreateReservationRequest request,
        @AuthenticationPrincipal PrincipalUser userInfo
    ) {

        CreateReservationResponse response = reservationService.createReservation(userInfo.getId(),
            shopId, request);

        return BaseResponse.ok(response, BaseStatus.CREATED);

    }

    @Operation(
        summary = "예약 수락",
        description = "reservationId에 해당하는 예약을 CONFIRMED 상태로 변경한다. (Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "예약 수락 성공"),
        }
    )
    @PatchMapping("/reservations/{reservationId}/confirm")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> confirmReservation(
        @PathVariable Long reservationId,
        @AuthenticationPrincipal PrincipalUser principal) {
        reservationService.confirmReservation(reservationId, principal.getId());

        return BaseResponse.ok(BaseStatus.OK);
    }

    @Operation(
        summary = "예약 거절",
        description = "reservationId에 해당하는 예약을 REFUSED 상태로 변경한다. (Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "예약 거절 성공"),
        }
    )
    @PatchMapping("/reservations/{reservationId}/refuse")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> refuseReservation(
        @PathVariable Long reservationId,
        @AuthenticationPrincipal PrincipalUser principal) {
        reservationService.refuseReservation(reservationId, principal.getId());

        return BaseResponse.ok(BaseStatus.OK);
    }

    @Operation(
        summary = "예약 취소",
        description = "reservationId에 해당하는 예약을 CANCELLED 상태로 변경한다. (Completed)",
        responses = {
            @ApiResponse(responseCode = "200", description = "예약 취소 성공"),
        }
    )
    @PatchMapping("/reservations/{reservationId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> cancelReservation(
        @PathVariable Long reservationId,
        @AuthenticationPrincipal PrincipalUser principal) {
        reservationService.cancelReservation(reservationId, principal.getId());

        return BaseResponse.ok(BaseStatus.OK);
    }

    // 1차 MVP 목표에서 제외
//    @GetMapping("/details")
//    public BaseResponse<AvailableTimeReservationResponse> getAvailableTimes(
//        @PathVariable Long shopId,
//        @RequestParam String date // yyyy-MM-dd
//    ){
//        AvailableTimeReservationResponse response = reservationService.getAvailableTimes(shopId, date);
//        return BaseResponse.ok(response, HttpStatus.OK);
//    }


}
