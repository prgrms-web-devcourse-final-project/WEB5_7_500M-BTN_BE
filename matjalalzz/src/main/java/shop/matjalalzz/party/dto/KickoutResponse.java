package shop.matjalalzz.party.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파티원 강제 퇴장 응답")
public record KickoutResponse(
    @Schema(description = "파티 모집이 완료된 상태에서 파티원을 강제 퇴장했을 때, 파티의 최소 인원 불만족으로 인한 파티 삭제 여부")
    boolean isPartyDeleted
) {

}
