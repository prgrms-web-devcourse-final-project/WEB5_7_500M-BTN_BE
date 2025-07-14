package shop.matjalalzz.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import shop.matjalalzz.comment.dto.CommentCreateRequest;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
            .commentId(comment.getId())
            .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt())
            .writer(CommentResponse.Writer.builder()
                .userId(comment.getWriter().getId())
                .nickname(comment.getWriter().getNickname())
                .build())
            .build();
    }

    public static Comment fromCommentCreateRequest(CommentCreateRequest request, Comment parent,
        Party party,
        User writer) {
        return Comment.builder()
            .content(request.content())
            .parent(parent)
            .party(party)
            .writer(writer)
            .build();
    }
}
