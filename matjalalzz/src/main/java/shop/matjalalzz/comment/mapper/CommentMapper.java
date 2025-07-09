package shop.matjalalzz.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.comment.dto.CommentCreateRequest;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.mock.MockParty;
import shop.matjalalzz.mock.MockUser;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

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
