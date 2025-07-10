package shop.matjalalzz.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record MyInfoResponse(
    @Schema(example = "minji97@gmail.com")
    String email,

    @Schema(example = "맛잘알민지")
    String nickname,

    @Schema(example = "USER")
    String role,

    @Schema(example = "김민지")
    String name,

    @Schema(example = "28")
    int age,

    @Schema(example = "W", description = "성별: M or W")
    String gender,

    @Schema(example = "1800")
    int point,

    @Schema(example = "010-1234-5678")
    String phoneNumber,

    @Schema(example = "UUID_a.png")
    String bucketId,

    @Schema(example = "https://s3.amazonaws.com/bucket/uploads/reviews/UUID_a.png")
    String profile
) {

}
