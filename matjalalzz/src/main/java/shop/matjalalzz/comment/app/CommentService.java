package shop.matjalalzz.comment.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.comment.dao.CommentRepository;
import shop.matjalalzz.comment.dto.CommentCreateRequest;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.comment.mapper.CommentMapper;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.dao.PartyRepository;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PartyRepository partyRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(CommentCreateRequest request, Long partyId,
        Long writerId) {
        //TODO: 개선 필요
        Party party = partyRepository.findById(partyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND)); //TODO: 개선
        User writer = userRepository.findById(writerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND)); //TODO: 개선
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
            () -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
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
