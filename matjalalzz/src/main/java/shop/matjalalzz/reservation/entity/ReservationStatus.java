package shop.matjalalzz.reservation.entity;

public enum ReservationStatus {
    PENDING, // 예약 생성됨(대기 상태)
    CANCELLED, // 예약 취소됨(예약자가 취소한 경우)
    CONFIRMED,// 에약 승인됨(사장이 예약을 승인한 경우)
    REFUSED,// 예약 거절됨(사장이 예약을 거절한 경우)
    TERMINATED,// 예약이 정상적으로 종료됨
}
