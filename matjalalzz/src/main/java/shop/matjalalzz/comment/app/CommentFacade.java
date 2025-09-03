package shop.matjalalzz.comment.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.comment.dto.CommentCreateRequest;
import shop.matjalalzz.comment.dto.CommentResponse;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.comment.mapper.CommentMapper;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.inquiry.app.InquiryFacade;
import shop.matjalalzz.inquiry.app.query.InquiryQueryService;
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.dto.InquiryOneGetResponse;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@Service
@RequiredArgsConstructor
public class CommentFacade {

    private final UserService userService;
    private final InquiryQueryService inquiryQueryService;
    private final PartyService partyService;
    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;

    @Transactional
    public CommentResponse createComment(CommentCreateRequest request, Long partyId,
        Long writerId) {
        Party party = partyService.findById(partyId);
        User writer = userService.getUserById(writerId);
        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentQueryService.getComment(request.parentId());
        }
        Comment comment = CommentMapper.fromCommentCreateRequest(request, parent, party, writer);
        Comment result = commentCommandService.save(comment);
        return validateMap(result);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentQueryService.getComment(commentId);
        validatePermission(comment, userId);
        commentCommandService.delete(comment);
    }

    @Transactional
    public CommentResponse createInquiryComment(CommentCreateRequest request, Long inquiryId, Long writerId) {
        Inquiry inquiry = inquiryQueryService.getOneInquiry(inquiryId).orElseThrow(()->new BusinessException(ErrorCode.NOT_FIND_INQUIRY));
        User user = userService.getUserById(writerId);

        // 관리자이거나 자신이 작성한 문의글인 경우 댓글 작성
        adminOrWriter(user, inquiry);

        Comment parent = null;
        if (request.parentId() != null) {
            parent = commentQueryService.getComment(request.parentId());
        }
        Comment comment = CommentMapper.fromInquiryCommentCreateRequest(request, parent, inquiry, user);
        Comment result = commentCommandService.save(comment);
        return validateMap(result);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByInquiry(Long inquiryId, Long userId) {
        User user = userService.getUserById(userId);
        Inquiry inquiry = inquiryQueryService.getOneInquiry(inquiryId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_INQUIRY));

        // 관리자이거나 자신이 작성한 문의글인 경우 댓글 조회
        adminOrWriter(user, inquiry);

        List<Comment> comments = commentQueryService.findCommentsByInquiry(inquiryId);
        return comments.stream().map(this::validateMap).toList();

    }

    @Transactional(readOnly = true)
    public CommentResponse findComment(Long commentId) {
        Comment comment = commentQueryService.getComment(commentId);
        return validateMap(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByParty(Long partyId) {
        List<Comment> comments = commentQueryService.findCommentsByParty(partyId);
        return comments.stream().map(this::validateMap).toList();
    }

    private void adminOrWriter(User user, Inquiry inquiry) {
        //관리자가 아니면서 자신이 쓴 문의글도 아니면 예외 발생
        if (user.getRole() != Role.ADMIN && !inquiry.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private CommentResponse validateMap(Comment comment) {
        Long parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        return CommentMapper.toCommentResponse(comment, parentId);
    }

    private void validatePermission(Comment comment, Long actorId) {
        if (!comment.getWriter().getId().equals(actorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
