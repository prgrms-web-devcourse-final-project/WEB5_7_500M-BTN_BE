package shop.matjalalzz.reservation.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 상태 값")
public enum ReservationStatus {

    @Schema(description = "예약 생성됨(대기 상태)")
    PENDING,

    @Schema(description = "예약자가 취소함")
    CANCELLED,

    @Schema(description = "사장이 예약을 승인함")
    CONFIRMED,

    @Schema(description = "사장이 예약을 거절함")
    REFUSED,

    @Schema(description = "예약이 정상적으로 종료됨")
    TERMINATED
}

