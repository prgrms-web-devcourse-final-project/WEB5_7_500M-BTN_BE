package shop.matjalalzz.reservation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.reservation.app.ReservationService;
import shop.matjalalzz.reservation.dto.CreateReservationRequest;
import shop.matjalalzz.reservation.dto.CreateReservationResponse;
import shop.matjalalzz.reservation.dto.ReservationListResponse;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/shops/{shopId}/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
        summary = "식당 예약 목록 조회",
        description = "shopId에 해당하는 식당의 예약 목록을 필터와 커서 기반으로 조회한다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공",
                content = @Content(schema = @Schema(implementation = ReservationListResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 shopId")
        }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<ReservationListResponse> getReservations(
        @PathVariable Long shopId,
        @RequestParam(defaultValue = "TOTAL") String filter,
        @RequestParam Long cursor,
        @RequestParam(defaultValue = "10") int size
    ) {
        ReservationListResponse response = reservationService.getReservations(shopId, filter,
            cursor, size);

        return BaseResponse.ok(response, BaseStatus.OK);
    }

    @Operation(
        summary = "예약 생성",
        description = "shopId에 해당하는 식당에 예약을 생성한다. 파티 예약인 경우 partyId를 쿼리 파라미터로 전달해야 한다.",
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<CreateReservationResponse> createReservation(
        @PathVariable Long shopId,
        @RequestParam(required = false) Long partyId, // party 여부 확인 및 party가 있는 경우 id 받아오는 용
        @Valid @RequestBody CreateReservationRequest request
    ) {
        CreateReservationResponse response = reservationService.createReservation(shopId, partyId, request);

        return BaseResponse.ok(response, BaseStatus.CREATED);

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
