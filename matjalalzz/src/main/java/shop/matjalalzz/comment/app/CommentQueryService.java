package shop.matjalalzz.comment.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.comment.dao.CommentRepository;
import shop.matjalalzz.comment.entity.Comment;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
            () -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }

    public List<Comment> findCommentsByParty(Long partyId) {
        return commentRepository.findAllByPartyId(partyId);
    }

    public List<Comment> findCommentsByInquiry(Long inquiryId) {
        return commentRepository.findAllByInquiryId(inquiryId);
    }

    public int findCommentSize(long inquiryId){
        return commentRepository.findAllByInquiryId(inquiryId).size();
    }
}
