package shop.matjalalzz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = "shop.matjalalzz")
@EnableDiscoveryClient
@EnableScheduling
public class MatjalalzzApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatjalalzzApplication.class, args);
    }

}
