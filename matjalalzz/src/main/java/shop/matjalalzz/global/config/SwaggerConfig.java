package shop.matjalalzz.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${custom.cors.backend-origin}")
    private String backendUrl;

    @Value("${server.port}")
    private String backendPort;

    @Bean
    public OpenAPI api() {
        Info info = new Info()
            .version("v1.0.0")
            .title("맛잘알 API")
            .description("엑세스 토큰은 발급 후 헤더에 들어가고 리프래쉬 토큰은 HTTP ONLY 쿠키에 들어갑니다");

        List<Server> servers = List.of(
            new Server().url(backendUrl),
            new Server().url("https://localhost:" + backendPort)
        );

        String accessScheme = "accessToken";
        String refreshScheme = "refreshToken";

        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList(accessScheme)
            .addList(refreshScheme);

        // SecuritySchemes 등록
        Components components = new Components()
            .addSecuritySchemes(accessScheme, new SecurityScheme()
                .name(accessScheme)
                .type(Type.HTTP) // HTTP 방식
                .scheme("bearer")
                .in(In.HEADER)
                .bearerFormat("Authorization"))
            .addSecuritySchemes(refreshScheme, new SecurityScheme()
                .name(refreshScheme)
                .type(Type.APIKEY)
                .in(In.COOKIE)
            );

        return new OpenAPI()
            .info(info)
            .addSecurityItem(securityRequirement)
            .components(components)
            .servers(servers);
    }
}
