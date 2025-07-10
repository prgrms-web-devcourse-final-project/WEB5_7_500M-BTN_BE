package shop.matjalalzz.party.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import shop.matjalalzz.party.entity.enums.GenderCondition;

public record PartyCreateRequest(
    @NotBlank @Schema(description = "게시글 제목") @Parameter(required = true) String title,
    @NotNull @Schema(description = "음식점 ID") @Parameter(required = true) Long shopId,
    @NotNull @Future @Schema(description = "모임 일시") @Parameter(required = true) LocalDateTime metAt,
    @NotNull @Future @Schema(description = "파티 모집 마감 일시") @Parameter(required = true) LocalDateTime deadline,
    @NotBlank @Schema(description = "모집 성별(여자, 남자, 무관 중 택 1") @Parameter(required = true) GenderCondition genderCondition,
    @Schema(description = "모집 최소 나이") @Min(value = 0) int minAge,
    @Schema(description = "모집 최대 나이") int maxAge,
    @NotNull @Min(value = 1) @Schema(description = "모집 최소 인원") @Parameter(required = true) int minCount,
    @NotNull @Schema(description = "모집 최대 인원") @Parameter(required = true) int maxCount,
    @Schema(description = "게시글 본문") String description
) {

}
