package shop.matjalalzz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "shop.matjalalzz.tosspay") // TossApiClient 있는 패키지
@EnableScheduling
public class MatjalalzzApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatjalalzzApplication.class, args);
    }

}
