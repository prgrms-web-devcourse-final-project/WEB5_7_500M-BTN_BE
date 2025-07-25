package shop.matjalalzz.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import shop.matjalalzz.chat.interceptor.StompAuthInterceptor;
import shop.matjalalzz.chat.interceptor.StompLoggingInterceptor;
import shop.matjalalzz.chat.interceptor.StompSubscriptionInterceptor;
import shop.matjalalzz.chat.interceptor.WebsocketAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompLoggingInterceptor stompLoggingInterceptor;
    private final StompAuthInterceptor stompAuthInterceptor;
    private final WebsocketAuthInterceptor websocketAuthInterceptor;
    private final StompSubscriptionInterceptor stompSubscriptionInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .addInterceptors(websocketAuthInterceptor)
            .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompLoggingInterceptor);
        registration.interceptors(stompAuthInterceptor);
        registration.interceptors(stompSubscriptionInterceptor);
    }
}
