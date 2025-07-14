package shop.matjalalzz.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "내 정보 수정 요청")
public record MyInfoUpdateRequest(
    @Schema(example = "맛잘알민지")
    @Max(20)
    String nickname,

    @Schema(example = "010-1234-5678")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$")
    String phoneNumber,

    @Schema(example = "28")
    @Min(0)
    Integer age,

    @Schema(example = "UUID_b.png")
    String bucketId,

    @Schema(example = "https://s3.amazonaws.com/bucket/uploads/reviews/UUID_b.png")
    String profile
) {

}