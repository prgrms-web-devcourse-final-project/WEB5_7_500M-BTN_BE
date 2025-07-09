package shop.matjalalzz.party.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import shop.matjalalzz.party.entity.enums.GenderCondition;

public record PartyCreateRequest(
    @NotBlank String title,
    @NotBlank Long shopId,
    @NotBlank LocalDateTime metAt,
    @NotBlank LocalDateTime deadline,
    @NotBlank GenderCondition genderCondition,
    @NotBlank @Min(value = 0) int minAge,
    @NotBlank int maxAge,
    @NotBlank @Min(value = 1) int minCount,
    @NotBlank int maxCount,
    String description
) {

}
