package shop.matjalalzz.global.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import shop.matjalalzz.global.exception.domain.ErrorCode;


@Getter
public class OAuth2Exception extends AuthenticationException {

    private final ErrorCode errorCode;

    public OAuth2Exception(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
