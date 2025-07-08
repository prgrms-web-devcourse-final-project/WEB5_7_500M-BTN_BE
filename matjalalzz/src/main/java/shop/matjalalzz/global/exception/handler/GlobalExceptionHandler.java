package shop.matjalalzz.global.exception.handler;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.BaseErrorCode;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.exception.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 비즈니스 로직에서 발생한 예외 처리 (ErrorCode 기반)
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
		BaseErrorCode code = e.getErrorCode();

		String path = request.getMethod() + " " + request.getRequestURI();

		return ResponseEntity.status(code.getStatus()).body(ErrorResponse.builder()
			.status(code.getStatus().value())
			.code(code.getCode())
			.message(code.getMessage())
			.path(path)
			.build());
	}


}
