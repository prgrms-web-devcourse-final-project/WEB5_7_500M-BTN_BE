package shop.matjalalzz.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class JpaConfig {

}
