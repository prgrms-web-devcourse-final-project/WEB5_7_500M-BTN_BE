package shop.matjalalzz.tosspay.config;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmRequest;
import shop.matjalalzz.tosspay.dto.TossPaymentConfirmResponse;

@FeignClient(            // Feign을 통해 외부 HTTP API와 통신하기 위한 인터페이스 정의
	name = "tossClient",
	url = "https://api.tosspayments.com",
	configuration = TossFeignConfig.class // 요청에 필요한 공통 헤더 등을 설정할 수 있는 Feign 전용 설정 클래스
)

public interface TossApiClient {

	@PostMapping("/v1/payments/confirm")
		// Toss 결제 승인 API에 POST 요청
	TossPaymentConfirmResponse confirmPayment(
		// TossPaymentConfirmResponse에 Toss에게 받은 결제 승인 응답을 객체로 매핑해 반환
		// 이미 TossFeginConfig에서 Header에서 Key 셋팅하고 내보내고 있으므로 아래 RequestHeader는 불필요
		@RequestHeader("Authorization") String authHeader, // Toss에서 요구하는 Basic 인증 헤더를 전달
		@RequestBody TossPaymentConfirmRequest request        // Toss에 보낼 JSON 본문
	);

}
