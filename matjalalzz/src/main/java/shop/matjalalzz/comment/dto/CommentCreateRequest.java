package shop.matjalalzz.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CommentCreateRequest(
    
    @Schema(description = "부모 댓글 ID (대댓글인 경우)")
    Long parentId,

    @Schema(description = "댓글 내용")
    @NotBlank(message = "본문은 비어있을 수 없습니다.")
    String content
) {

}
