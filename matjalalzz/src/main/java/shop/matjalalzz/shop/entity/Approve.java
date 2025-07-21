package shop.matjalalzz.shop.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;

public enum Approve {
    PENDING, // 보류
    APPROVED, // 승인
    REJECTED; // 거부

    @JsonCreator // 외부 요청으로 상태를 바꿀 때 심사
    public static Approve getApprove(String text) {
        try{
            Approve approve = Approve.valueOf(text.toUpperCase());
            if ( approve.equals(Approve.REJECTED) || approve.equals(Approve.APPROVED)) {
                return approve;
            }
        } catch (Exception e) {}
        throw new BusinessException(ErrorCode.INVALID_REQUEST_DATA);
    }
}
