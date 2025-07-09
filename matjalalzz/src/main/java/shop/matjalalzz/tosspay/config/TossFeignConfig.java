package shop.matjalalzz.tosspay.config;

import feign.RequestInterceptor;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// Toss API 호출 시 공통 헤더를 자동으로 추가해주는 Feign 클라이언트용 설정 클래스
//Spring 백엔드 서버가 Toss 서버에 결제 승인 요청을 보낼 때 이 TossFeignConfig에서 설정한 인증 헤더가 자동으로 붙음
//Toss 서버에서 백엔드 서버의 신원을 인증하기 위해 헤더를 사용
public class TossFeignConfig {

	@Value("${toss.secret-key}")
	private String secretKey;

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
			requestTemplate.header("Authorization", "Basic " + encodedKey);
			requestTemplate.header("Content-Type", "application/json");
		};
	}
}