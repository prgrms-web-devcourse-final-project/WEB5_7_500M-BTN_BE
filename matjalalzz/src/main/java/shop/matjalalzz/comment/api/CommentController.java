package shop.matjalalzz.comment.api;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.comment.app.CommentCommandService;
import shop.matjalalzz.comment.app.CommentQueryService;
import shop.matjalalzz.comment.dto.CommentCreateRequest;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.security.PrincipalUser;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentControllerSpec {

    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;

    @Override
    public BaseResponse<List<CommentResponse>> getComments(@PathVariable Long partyId) {
        return BaseResponse.ok(commentQueryService.findCommentsByParty(partyId), BaseStatus.OK);
    }

    @Override
    public BaseResponse<Void> createComment(
        @PathVariable Long partyId, @Valid @RequestBody CommentCreateRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {
        commentCommandService.createComment(request, partyId, principal.getId());
        return BaseResponse.ok(BaseStatus.CREATED);
    }

    @Override
    public void deleteComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal PrincipalUser principal) {
        commentCommandService.deleteComment(commentId, principal.getId());
    }

    @Override
    public BaseResponse<List<CommentResponse>> getInquiryComments(@PathVariable Long inquiryId,
        @AuthenticationPrincipal PrincipalUser principal) {
        return BaseResponse.ok(
            commentQueryService.findCommentsByInquiry(inquiryId, principal.getId()),
            BaseStatus.OK);
    }

    @Override
    public BaseResponse<Void> createInquiryComment(
        @PathVariable Long inquiryId, @Valid @RequestBody CommentCreateRequest request,
        @AuthenticationPrincipal PrincipalUser principal) {
        commentCommandService.createInquiryComment(request, inquiryId, principal.getId());
        return BaseResponse.ok(BaseStatus.CREATED);
    }


}
