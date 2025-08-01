package shop.matjalalzz.global.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class CookieUtils {

    public static void setRefreshTokenCookie(HttpServletResponse response, String refreshToken,
        int tokenTTL) {

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(tokenTTL)
            .sameSite("None")
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public static void deleteRefreshTokenCookie(HttpServletResponse response) {

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(0)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
}
