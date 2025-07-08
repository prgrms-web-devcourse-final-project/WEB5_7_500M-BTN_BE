package shop.matjalalzz.domain.party.dto;

import java.time.LocalDateTime;
import shop.matjalalzz.domain.party.entity.GenderCondition;

public record PartyCreateRequest(
	String title,
	Long shopId,
	LocalDateTime metAt,
	LocalDateTime deadline,
	GenderCondition genderCondition,
	int minAge,
	int maxAge,
	int minCount,
	int maxCount,
	String description
) {

}
