package shop.matjalalzz.global.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI api() {
        Info info = new Info()
            .version("v1.0.0")
            .title("맛잘알 API")
            .description("엑세스 토큰은 발급 후 헤더에 들어가고 리프래쉬 토큰은 HTTP ONLY 쿠키에 들어갑니다");

        // SecuritySecheme명
        String jwtSchemeName = "JwtAuth";
        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        // SecuritySchemes 등록
        Components components = new Components()
            .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP) // HTTP 방식
                .scheme("bearer")
                .in(SecurityScheme.In.HEADER)
                .bearerFormat("Authorization")); // 토큰 형식을 지정하는 임의의 문자(Optional)


        return new OpenAPI()
            .info(info)
            .addSecurityItem(securityRequirement)
            .components(components);

    }
}
