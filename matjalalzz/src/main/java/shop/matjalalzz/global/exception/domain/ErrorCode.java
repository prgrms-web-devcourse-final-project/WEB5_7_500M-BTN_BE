package shop.matjalalzz.global.exception.domain;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    /*
     * Commons : 공통 예외 처리
     */
    // 400
    INVALID_REQUEST_DATA(HttpStatus.BAD_REQUEST, "요청 데이터가 올바르지 않습니다. 입력 데이터를 확인해 주세요."),
    INVALID_PAGINATION_PARAMETER(HttpStatus.BAD_REQUEST,
        "요청 파라미터가 유효하지 않습니다. page는 1 이상, size는 1 이상 50 이하로 설정 해야 합니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "잘못된 예약 상태입니다."),

    // 401
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요한 요청입니다. 로그인 해주세요."),


    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 오류가 발생했습니다."),





    /*
     * Auth : 로그인 / 인증 관련 예외 처리
     */

    // 401 Unauthorized
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access Token이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    INVALID_PROVIDER(HttpStatus.UNAUTHORIZED, "유효하지 않은 OAuth2.0 제공자입니다."),
    TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "서버에 저장된 토큰과 일치하지 않습니다."),
    BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다. 다시 로그인 해주세요."),
    AUTH_HEADER_MISSING(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 누락되었습니다."),

    // 403 Forbidden
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "해당 리소스에 접근할 권한이 없습니다."),

    // 404 Not Found
    LOGIN_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "로그인 정보와 일치하는 사용자가 존재하지 않습니다."),

    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "예약 시간이 유효하지 않습니다."),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터가 존재하지 않습니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "음식점을 찾을 수 없습니다."),
    PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "파티를 찾을 수 없습니다."),
    // 409 Conflict
    DUPLICATE_DATA(HttpStatus.CONFLICT, "데이터가 중복 되었습니다."),


    /*
     * toss pay
     */

    //400,
    ZERO_AMOUNT_PAYMENT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "0원 결제는 허용되지 않습니다."),

    /**
     * party
     */
    INVALID_DEADLINE(HttpStatus.BAD_REQUEST, "파티 마감일자는 모임일자보다 이전이어야 합니다."),
    ALREADY_PARTY_USER(HttpStatus.BAD_REQUEST, "이미 파티에 참여 중입니다."),
    NOT_JOIN_PARTY(HttpStatus.BAD_REQUEST, "파티에 참여 중이 아닙니다."),
    HOST_CANNOT_QUIT_PARTY(HttpStatus.BAD_REQUEST, "호스트는 파티에 탈퇴할 수 없습니다."),
    FORBIDDEN_ACCESS_DELETE_PARTY(HttpStatus.FORBIDDEN, "호스트만 파티를 삭제할 수 있습니다."),
    CANNOT_COMPLETE_PARTY(HttpStatus.BAD_REQUEST, "호스트만 파티 상태 변경이 가능합니다."),
    INVALID_AGE_CONDITION(HttpStatus.BAD_REQUEST, "최대 나이는 최소 나이보다 많아야 합니다."),
    INVALID_COUNT_CONDITION(HttpStatus.BAD_REQUEST, "최대 인원은 최소 인원보다 많아야 합니다."),
    NOT_RECRUITING_PARTY(HttpStatus.BAD_REQUEST, "현재 모집 중인 파티가 아닙니다."),
    FULL_COUNT_PARTY(HttpStatus.BAD_REQUEST, "파티 정원이 이미 가득 찼습니다."),
    DEADLINE_GONE(HttpStatus.BAD_REQUEST, "파티 모집 마감 시간이 지났습니다."),
    NOT_MATCH_GENDER(HttpStatus.BAD_REQUEST, "요청자의 성별이 파티 조건과 맞지 않습니다."),
    NOT_MATCH_AGE(HttpStatus.BAD_REQUEST, "요청자의 나이가 파티 조건에 맞지 않습니다."),
    ALREADY_COMPLETE_PARTY(HttpStatus.BAD_REQUEST, "이미 모집 완료되었거나 종료된 파티입니다."),
    CANNOT_DELETE_PARTY_STATUS(HttpStatus.BAD_REQUEST, "모집 완료되거나 종료된 파티는 삭제할 수 없습니다."),

    /**
     * shop
     */
    DUPLICATE_SHOP(HttpStatus.CONFLICT, "이미 존재하는 음식점 정보입니다 수정으로 정보를 넣어주세요."),
    NOT_FIND_SHOP(HttpStatus.NOT_FOUND,"음식점 정보가 존재하지 않습니다."),
    NOT_SHOP_OWNER(HttpStatus.BAD_REQUEST, "상점 주인이 아닙니다"),

    /**
     * image
     */
    // 여러 이미지 저장 시 하나의 이미지라도 저장에 실패하면 안됨
    IMAGE_SAVE_FAILED(HttpStatus.BAD_REQUEST, "이미지 저장에 실패하였습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}