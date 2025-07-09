package shop.matjalalzz.domain.comment.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CommentResponse(
    Long commentId,
    Long parentId,
    String content,
    LocalDateTime createdAt,
    Writer writer) {

    @Builder
    public record Writer(Long userId, String nickname) {

    }
}

