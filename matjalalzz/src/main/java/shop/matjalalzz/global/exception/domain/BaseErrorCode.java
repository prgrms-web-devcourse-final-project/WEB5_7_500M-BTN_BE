package shop.matjalalzz.global.exception.domain;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

    String getMessage();

    HttpStatus getStatus();

    String getCode();
}
