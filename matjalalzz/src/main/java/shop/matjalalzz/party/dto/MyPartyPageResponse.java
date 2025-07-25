package shop.matjalalzz.party.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "내 파티 목록 응답")
public record MyPartyPageResponse(
    @Schema(description = "파티 목록") List<MyPartyResponse> content,
    @Schema(description = "다음 커서 ID", example = "10") Long nextCursor
) {
}