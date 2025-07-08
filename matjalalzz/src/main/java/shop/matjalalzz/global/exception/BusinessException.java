package shop.matjalalzz.global.exception;

import lombok.Getter;
import shop.matjalalzz.global.exception.domain.ErrorCode;


@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
