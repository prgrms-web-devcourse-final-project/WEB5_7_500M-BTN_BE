package shop.matjalalzz.party.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;

@Builder
@Schema(description = "파티 정보 요약")
public record MyPartyResponse(
    @Schema(description = "파티 ID", example = "9")
    long partyId,

    @Schema(description = "파티 제목", example = "신전떡볶이 먹을 사람?")
    String title,

    @Schema(description = "가게 이름", example = "신전떡볶이 강남점")
    String shopName,

    @Schema(description = "만남 일시", example = "2025-07-10T18:00:00")
    LocalDateTime metAt,

    @Schema(description = "모집 마감 시간", example = "2025-07-09T18:00:00")
    LocalDateTime deadline,

    @Schema(description = "파티 상태", example = "COMPLETED")
    PartyStatus status,

    @Schema(description = "최대 인원", example = "5")
    int maxCount,

    @Schema(description = "최소 인원", example = "2")
    int minCount,

    @Schema(description = "현재 인원", example = "3")
    int currentCount,

    @Schema(description = "성별 조건", example = "M")
    GenderCondition genderCondition,

    @Schema(description = "최소 나이", example = "20")
    int minAge,

    @Schema(description = "최대 나이", example = "24")
    int maxAge,

    @Schema(description = "설명", example = "신전떡볶이 강남점은 뭔가 맛이 다르다는데, 가보실 분 구합니다!")
    String description,

    @Schema(description = "호스트 여부 (본인이 이 파티의 파티장인지 확인)", example = "true")
    boolean isHost
) {

}