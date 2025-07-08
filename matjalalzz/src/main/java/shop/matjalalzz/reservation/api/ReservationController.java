package shop.matjalalzz.reservation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.unit.BaseResponse;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;

@RestController("/shops/{shopId}/reservations")
@RequiredArgsConstructor
@Validated
public class ReservationController {

//    private final ReservationService reservationService;

    @Operation(
        summary = "예약 목록 조회",
        description = "shopId에 해당하는 식당의 예약 목록을 필터와 커서 기반으로 조회한다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ReservationListResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 shopId",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
        }
    )
    @GetMapping
    public BaseResponse<ReservationListResponse> getReservations(
        @PathVariable Long shopId,
        @RequestParam(required = false, defaultValue = "TOTAL") String filter,
        @RequestParam(required = false) Long cursor,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        ReservationListResponse response = reservationService.getReservations(shopId, filter,
            cursor);
        return BaseResponse.ok(response, HttpStatus.OK);
    }

    @Operation(
        summary = "예약 생성",
        description = "shopId에 해당하는 식당에 예약을 생성한다.",
        responses = {
            @ApiResponse(responseCode = "201", description = "예약 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "식당을 찾을 수 없음")
        }
    )
    @PostMapping
    public BaseResponse<CreateReservationResponse> createReservation(
        @PathVariable Long shopId,
        @Valid @RequestBody CreateReservationRequest request
    ) {
        CreateReservationResponse response = reservationService.createReservation(shopId, request);

        return BaseResponse.ok(response, HttpStatus.CREATED);

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
