package shop.matjalalzz.chat.interceptor;

import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.global.security.jwt.app.TokenProvider;
import shop.matjalalzz.global.security.jwt.dto.TokenBodyDto;
import shop.matjalalzz.global.security.oauth2.mapper.OAuth2Mapper;

@Component
@RequiredArgsConstructor
public class WebsocketAuthInterceptor implements HandshakeInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        URI uri = request.getURI();
        String token = UriComponentsBuilder.fromUri(uri)
            .build()
            .getQueryParams()
            .getFirst("token");

        if (tokenProvider.validate(token)) {
            TokenBodyDto tokenBodyDto = tokenProvider.parseAccessToken(token);
            PrincipalUser principalUser = OAuth2Mapper.toPrincipalUser(tokenBodyDto);

            attributes.put("principal", principalUser);

        } else {
            response.setStatusCode(ErrorCode.INVALID_ACCESS_TOKEN.getStatus());
            response.getBody().write(ErrorCode.INVALID_ACCESS_TOKEN.getMessage().getBytes());
            return false;
        }

        return true; // 연결 허용
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Exception exception) {

    }
}
