package shop.matjalalzz.global.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import shop.matjalalzz.global.security.filter.TokenAuthenticationFilter;
import shop.matjalalzz.global.security.handler.CustomAuthenticationEntryPoint;
import shop.matjalalzz.global.security.handler.OAuth2SuccessHandler;
import shop.matjalalzz.global.security.oauth2.app.OAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final CustomAuthenticationEntryPoint entryPoint;

    @Value("${custom.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
            .cors(cors -> cors.configurationSource(
                request -> {
                    CorsConfiguration configuration = new CorsConfiguration();

                    configuration.setAllowedOrigins(List.of(allowedOrigin));
                    configuration.setAllowedOriginPatterns(List.of("*"));
                    configuration.setAllowedMethods(
                        List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(List.of("*"));
                    configuration.setMaxAge(3600L);

                    configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
                    return configuration;
                }
            ))
            .csrf(AbstractHttpConfigurer::disable) // csrf 비활성
            .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 로그인 비활성
            .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성
            .logout(AbstractHttpConfigurer::disable) // 기본 로그아웃 비활성
            .addFilterAfter(tokenAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)
            .oauth2Login(oauth -> {
                oauth.userInfoEndpoint(userInfo ->
                        userInfo.userService(oAuth2UserService))
                    .successHandler(oAuth2SuccessHandler);
            })
            // 임시로 Admin만 권한 검사
            .authorizeHttpRequests(
                auth -> auth
                    ///reissue-token을 호출하는 시점에는 엑세스 토큰이 이미 만료되어 있으니 넣어야 함
                    .requestMatchers("/users/login", "/users/signup", "/users/reissue-token",
                        "swagger-ui.html", "users/authorization-info", "/v3/api-docs/**",
                        "/swagger-ui/**", "/error", "/oauth2/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/parties/{partyId}", "/parties").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")

                    .requestMatchers(HttpMethod.GET, "/reservations").hasRole("OWNER")
                    .requestMatchers(HttpMethod.PATCH,"/reservations/{reservationId}/**").hasRole("OWNER")


                    .requestMatchers(HttpMethod.GET, "/shops/{shopId}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/shops").permitAll()
                    .requestMatchers(HttpMethod.GET, "/shops/search").permitAll()
                    .requestMatchers("/owner/**").hasAnyRole("OWNER", "ADMIN")

                    .requestMatchers(HttpMethod.GET, "/parties/{partyId}/comments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/shops/{shopId}/reviews").permitAll()

                    //.anyRequest().permitAll(); //전부 다 허용하는 테스트용

                    .anyRequest().hasAnyRole("USER", "ADMIN") //나머지 요청은 USER 또는 ADMiN 권한을 가져야 접근 가능
            )
            .exceptionHandling(handler ->
                handler.authenticationEntryPoint(entryPoint))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
