package shop.matjalalzz.comment.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import shop.matjalalzz.comment.dto.CommentCreateRequest;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.security.PrincipalUser;

@Tag(name = "댓글 API", description = "댓글 관련 API")
public interface CommentControllerSpec {

    @Operation(summary = "댓글 조회", description = "특정 모임의 댓글 목록을 조회합니다.(Completed)")
    @GetMapping("/parties/{partyId}/comments")
    @ResponseStatus(HttpStatus.OK)
    BaseResponse<List<CommentResponse>> getComments(Long partyId);

    @Operation(summary = "댓글 작성", description = "특정 모임에 댓글을 작성합니다.(Completed)")
    @PostMapping("/parties/{partyId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    BaseResponse<Void> createComment(Long partyId, CommentCreateRequest request,
        PrincipalUser principal);

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.(Completed)")
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(Long commentId, PrincipalUser principal);

    @Operation(summary = "고객센터 댓글 조회", description = "특정 모임의 댓글 목록을 조회합니다.(Completed)")
    @GetMapping("/inquiry/{inquiryId}/comments")
    @ResponseStatus(HttpStatus.OK)
    BaseResponse<List<CommentResponse>> getInquiryComments(Long inquiryId, PrincipalUser principal);

    @Operation(summary = "고객센터 댓글 작성", description = "특정 모임에 댓글을 작성합니다.(Completed)")
    @PostMapping("/inquiry/{inquiryId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    BaseResponse<Void> createInquiryComment(Long inquiryId, CommentCreateRequest request,
        PrincipalUser principal);
}
