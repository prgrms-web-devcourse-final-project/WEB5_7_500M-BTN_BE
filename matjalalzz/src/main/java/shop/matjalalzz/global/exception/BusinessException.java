package shop.matjalalzz.global.exception;

import lombok.Getter;
import shop.matjalalzz.global.exception.domain.BaseErrorCode;
import shop.matjalalzz.global.exception.domain.ErrorCode;


@Getter
public class BusinessException extends RuntimeException {

	private final BaseErrorCode errorCode;

	public BusinessException(BaseErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
