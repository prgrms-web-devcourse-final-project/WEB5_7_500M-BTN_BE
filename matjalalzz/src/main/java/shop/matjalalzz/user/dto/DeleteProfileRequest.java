package shop.matjalalzz.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeleteProfileRequest(
    @NotBlank
    @Schema(example = "/profile/52/52_img")
    String profileKey
) {

}
