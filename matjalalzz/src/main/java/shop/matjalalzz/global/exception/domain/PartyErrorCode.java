package shop.matjalalzz.global.exception.domain;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PartyErrorCode implements BaseErrorCode{

	SHOP_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 음식점입니다."), //추후 ShopErrorCode로 이동 필요
	INVALID_DEADLINE(HttpStatus.BAD_REQUEST, "파티 마감시간은 파티 일시 이전이어야 합니다."),
	PARTY_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않은 파티입니다.");

	private final HttpStatus status;
	private final String message;

	PartyErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public String getCode() {
		return this.name();
	}
}
