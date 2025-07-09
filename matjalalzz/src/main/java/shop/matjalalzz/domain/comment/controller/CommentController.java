package shop.matjalalzz.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.domain.comment.dto.CommentCreateRequest;
import shop.matjalalzz.domain.comment.dto.CommentResponse;
import shop.matjalalzz.domain.comment.entity.Comment;
import shop.matjalalzz.domain.comment.mapper.CommentMapper;
import shop.matjalalzz.global.unit.BaseResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "댓글 API", description = "댓글 관련 API")
public class CommentController {

    @Operation(summary = "댓글 조회", description = "특정 모임의 댓글 목록을 조회합니다.")
    @GetMapping("/parties/{partyId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<List<CommentResponse>> getComments(
        @PathVariable Long partyId) {
        return BaseResponse.ok(List.of(CommentMapper.toCommentResponse(Comment.builder().build())),
            HttpStatus.OK);
    }

    @Operation(summary = "댓글 작성", description = "특정 모임에 댓글을 작성합니다.")
    @PostMapping("/parties/{partyId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<Void> createComment(
        @PathVariable Long partyId,
        @RequestBody CommentCreateRequest request,
        Authentication authentication) {
        return BaseResponse.okOnlyStatus(HttpStatus.CREATED);
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
        @PathVariable Long commentId,
        Authentication authentication) {
    }

}
