package shop.matjalalzz.domain.comment.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.domain.comment.dao.CommentRepository;
import shop.matjalalzz.domain.comment.dto.CommentCreateRequest;
import shop.matjalalzz.domain.comment.dto.CommentResponse;
import shop.matjalalzz.domain.comment.entity.Comment;
import shop.matjalalzz.domain.comment.mapper.CommentMapper;
import shop.matjalalzz.domain.mock.MockParty;
import shop.matjalalzz.domain.mock.MockUser;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse createComment(CommentCreateRequest request, Long partyId,
        Long writerId) {
        MockParty party = MockParty.builder().id(partyId).build();
        MockUser writer = MockUser.builder().id(writerId).build();
        Comment parent = null;
        if (request.parentId() != null) {
            parent = getComment(request.parentId());
        }
        Comment comment = CommentMapper.fromCommentCreateRequest(request, parent, party, writer);
        commentRepository.save(comment);
        return CommentMapper.toCommentResponse(comment);
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
            () -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public CommentResponse findComment(Long commentId) {
        Comment comment = getComment(commentId);
        return CommentMapper.toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getComment(commentId);
        validatePermission(comment, userId);
        commentRepository.delete(comment);
    }

    private void validatePermission(Comment comment, Long actorId) {
        if (!comment.getWriter().getId().equals(actorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByParty(Long partyId) {
        List<Comment> comments = commentRepository.findAllByPartyId(partyId);
        return comments.stream().map(CommentMapper::toCommentResponse).toList();
    }

}
