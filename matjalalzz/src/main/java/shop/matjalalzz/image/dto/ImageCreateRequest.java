package shop.matjalalzz.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record ImageCreateRequest(
    @Schema(description = "이미지명 리스트")
    @NotEmpty(message = "이미지는 최소 1개")
    List<@NotBlank(message = "이미지명은 공백일 수 없습니다.") String> imageNames
) {

}
