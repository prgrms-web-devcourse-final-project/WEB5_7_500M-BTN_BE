package shop.matjalalzz.party.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PartyMemberResponse(
    @Schema(description = "파티원 ID", example = "9")
    long userId,

    @Schema(description = "파티원 닉네임", example = "김치맨")
    String userNickname,

    @Schema(description = "파티원 프로필 이미지 URL", example = "https://team05-500m-btn.s3.ap-northeast-2.amazonaws.com/...")
    String userProfile,

    @Schema(description = "파티장 여부", example = "false")
    boolean isHost
) {

}
