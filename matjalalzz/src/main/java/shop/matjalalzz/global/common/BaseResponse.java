package shop.matjalalzz.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    private HttpStatus status;
    private T data;

    public BaseResponse(HttpStatus status) {
        this.status = status;
    }

    public BaseResponse(HttpStatus status, T data) {
        this.status = status;
        this.data = data;
    }

    //메시지도 주고 싶은 경우 (개발 초기 프론트와 연결 시 사용)
    //성공 메시지와 데이터 반환 (사용하지 않으면 null 넣어주기)
    public static <T> BaseResponse<T> ok( T data, HttpStatus status) {
        return new BaseResponse<>(status, data);
    }

    //성공 시 아무것도 반환하고 싶지 않을 때 사용
    public static BaseResponse<Void> okOnlyStatus(HttpStatus status) {
        return new BaseResponse<>(status);
    }

    //에러 메시지와 데이터를 반환 (사용하지 않으면 null 넣어주기)
    public static <T> BaseResponse<T> error( T data, HttpStatus status) {
        return new BaseResponse<>(status, data);
    }

    //에러 발생 시 아무것도 반환하고 싶지 않을 때 사용
    public static BaseResponse<Void> errorOnlyStatus(HttpStatus status) {
        return new BaseResponse<>(status);
    }

}