package shop.matjalalzz.comment.app;

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
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final PartyService partyService;
    private final UserService userService;
    private final InquiryRepository inquiryRepository;


    public CommentResponse createComment(CommentCreateRequest request, Long partyId,
        Long writerId) {
        Party party = partyService.findById(partyId);
        User writer = userService.getUserById(writerId);
        Comment parent = null;
        if (request.parentId() != null) {
            parent = getComment(request.parentId());
        }
        Comment comment = CommentMapper.fromCommentCreateRequest(request, parent, party, writer);
        Comment result = commentRepository.save(comment);
        return validateMap(result);
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getComment(commentId);
        validatePermission(comment, userId);
        comment.delete();
        comment.getChildren().forEach(Comment::delete);
    }

    public CommentResponse createInquiryComment(CommentCreateRequest request, Long inquiryId,
        Long writerId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_INQUIRY));
        User user = userService.getUserById(writerId);

        // 관리자이거나 자신이 작성한 문의글인 경우 댓글 작성
        adminOrWriter(user, inquiry);

        Comment parent = null;
        if (request.parentId() != null) {
            parent = getComment(request.parentId());
        }
        Comment comment = CommentMapper.fromInquiryCommentCreateRequest(request, parent, inquiry,
            user);
        Comment result = commentRepository.save(comment);
        return validateMap(result);
    }

    private void validatePermission(Comment comment, Long actorId) {
        if (!comment.getWriter().getId().equals(actorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private CommentResponse validateMap(Comment comment) {
        Long parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        return CommentMapper.toCommentResponse(comment, parentId);
    }

    private void adminOrWriter(User user, Inquiry inquiry) {
        if (!user.getRole().equals(Role.ADMIN) && !inquiry.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
            () -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }
}
