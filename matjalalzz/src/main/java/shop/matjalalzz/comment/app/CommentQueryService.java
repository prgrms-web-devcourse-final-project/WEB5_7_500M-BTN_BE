package shop.matjalalzz.comment.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.comment.dao.CommentRepository;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.comment.mapper.CommentMapper;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final InquiryRepository inquiryRepository;

    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
            () -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    public CommentResponse findComment(Long commentId) {
        Comment comment = getComment(commentId);
        return validateMap(comment);
    }

    public List<CommentResponse> findCommentsByParty(Long partyId) {
        List<Comment> comments = commentRepository.findAllByPartyId(partyId);
        return comments.stream().map(this::validateMap).toList();
    }

    public List<CommentResponse> findCommentsByInquiry(Long inquiryId, Long userId) {
        User user = userService.getUserById(userId);
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_INQUIRY));

        // 관리자이거나 자신이 작성한 문의글인 경우 댓글 조회
        adminOrWriter(user, inquiry);

        List<Comment> comments = commentRepository.findAllByInquiryId(inquiryId);
        return comments.stream().map(this::validateMap).toList();

    }

    private CommentResponse validateMap(Comment comment) {
        Long parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        return CommentMapper.toCommentResponse(comment, parentId);
    }

    private void adminOrWriter(User user, Inquiry inquiry) {
        //관리자가 아니면서 자신이 쓴 문의글도 아니면 예외 발생
        if (user.getRole() != Role.ADMIN && !inquiry.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
