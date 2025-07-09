package shop.matjalalzz.user.config;

//import io.security_JWT.backend.user.repository.BlackListRepository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import shop.matjalalzz.user.app.JwtTokenProvider;
import shop.matjalalzz.user.app.UserService;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtTokenProvider jwtTokenProvider,
        UserService userService,
        StringRedisTemplate stringRedisTemplate
        //, BlackListRepository blackListRepository

    ) throws Exception { //예외가 발생할 수 있는 코드 뜻
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtTokenProvider, userService,
            stringRedisTemplate //, blackListRepository
        );

        return http
            .formLogin(form -> form.disable())
            .csrf(csrf -> csrf.disable())

            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173")); // ✅ 프론트엔드 도메인
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*")); //프론트가 보내는 모든 헤더 허용
                config.setExposedHeaders(List.of("Authorization", "Refresh"));
                config.setAllowCredentials(true); // 쿠키나 인증 헤더 포함 요청 허용
                return config;
            }))

            .httpBasic(httpBasic -> httpBasic.disable())
            .authorizeHttpRequests(auth -> {
                auth
                    ///reissue-token을 호출하는 시점에는 엑세스 토큰이 이미 만료되어 있으니 넣어야 함
                    .requestMatchers("/user/login", "/user/signup", "/user/reissue-token",
                        "swagger-ui.html"
                        , "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")

//                    .anyRequest().permitAll(); //전부 다 허용하는 테스트용

                    .anyRequest().hasAnyRole("USER", "ADMIN"); //나머지 요청은 USER 또는 ADMiN 권한을 가져야 접근 가능
            })

            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}