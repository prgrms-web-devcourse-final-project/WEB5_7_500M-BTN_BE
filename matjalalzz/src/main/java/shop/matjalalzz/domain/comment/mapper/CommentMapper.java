package shop.matjalalzz.domain.comment.mapper;

import shop.matjalalzz.domain.comment.dto.CommentCreateRequest;
import shop.matjalalzz.domain.comment.dto.CommentResponse;
import shop.matjalalzz.domain.comment.entity.Comment;
import shop.matjalalzz.domain.mock.MockParty;
import shop.matjalalzz.domain.mock.MockUser;

public class CommentMapper {

    private CommentMapper() {
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
            .commentId(comment.getId())
            .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
            .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
            .createdAt(comment.getCreatedAt())
            .writer(CommentResponse.Writer.builder()
                .userId(comment.getWriter().getId())
                .nickname(comment.getWriter().getNickname())
                .build())
            .build();
    }

    public static Comment fromCommentCreateRequest(CommentCreateRequest request, MockParty party,
        MockUser writer) {
        return Comment.builder()
            .content(request.content())
            .party(party)
            .writer(writer)
            .build();
    }
}
