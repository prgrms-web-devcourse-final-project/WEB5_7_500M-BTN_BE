package shop.matjalalzz.global.exception.handler;


import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.OAuth2Exception;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.exception.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직에서 발생한 예외 처리 (ErrorCode 기반)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e,
        HttpServletRequest request) {
        ErrorCode code = e.getErrorCode();

        String path = request.getMethod() + " " + request.getRequestURI();

        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.builder()
            .status(code.getStatus().value())
            .code(code.name())
            .message(code.getMessage())
            .path(path)
            .build());
    }

    @ExceptionHandler(OAuth2Exception.class)
    public ResponseEntity<ErrorResponse> handleOAuth2Exception(OAuth2Exception e,
        HttpServletRequest request) {
        ErrorCode code = e.getErrorCode();

        String path = request.getMethod() + " " + request.getRequestURI();

        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.builder()
            .status(code.getStatus().value())
            .code(code.name())
            .message(code.getMessage())
            .path(path)
            .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e,
        HttpServletRequest request) {
        ErrorCode code = ErrorCode.AUTHENTICATION_REQUIRED;

        String path = request.getMethod() + " " + request.getRequestURI();

        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.builder()
            .status(code.getStatus().value())
            .code(code.name())
            .message(code.getMessage())
            .path(path)
            .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e,
        HttpServletRequest request) {
        ErrorCode code = ErrorCode.FORBIDDEN_ACCESS;

        String path = request.getMethod() + " " + request.getRequestURI();

        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.builder()
            .status(code.getStatus().value())
            .code(code.name())
            .message(code.getMessage())
            .path(path)
            .build());
    }
    //@ExceptionHandler(DataAccessException.class)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        ErrorCode code = ErrorCode.LOCK_FAILURE;

        String path = request.getMethod() + " " + request.getRequestURI();

        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.builder()
            .status(code.getStatus().value())
            .code(code.name())
            .message(code.getMessage())
            .path(path)
            .build());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException e, HttpServletRequest request) {
        log.error("토스 서버와의 통신에 실패하였습니다.");
        ErrorCode code = ErrorCode.TOSS_FEIGN_FAIL;

        String path = request.getMethod() + " " + request.getRequestURI();

        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.builder()
            .status(code.getStatus().value())
            .code(code.name())
            .message(code.getMessage())
            .path(path)
            .build());
    }

}
