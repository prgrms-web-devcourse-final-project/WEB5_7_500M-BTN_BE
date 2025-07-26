package shop.matjalalzz.party.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import shop.matjalalzz.party.entity.enums.GenderCondition;

public record PartyCreateRequest(
    @NotBlank @Schema(description = "게시글 제목")
    String title,

    @NotNull @Schema(description = "음식점 ID")
    Long shopId,

    @NotNull @Future @Schema(description = "모임 일시")
    LocalDateTime metAt,

    @NotNull @Future @Schema(description = "파티 모집 마감 일시")
    LocalDateTime deadline,

    @NotBlank @Schema(description = "모집 성별(W(여자), M(남자), A(무관) 중 택 1")
    GenderCondition genderCondition,

    @Schema(description = "모집 최소 나이") @Min(value = 0)
    int minAge,

    @Schema(description = "모집 최대 나이") @Min(value = 0)
    int maxAge,

    @NotNull @Min(value = 2) @Max(value = 10) @Schema(description = "파티 최소 인원(파티 호스트 포함)")
    int minCount,

    @NotNull @Min(value = 2) @Max(value = 10) @Schema(description = "파티 최대 인원(파티 호스트 포함)")
    int maxCount,

    @Schema(description = "게시글 본문")
    String description
) {

}
