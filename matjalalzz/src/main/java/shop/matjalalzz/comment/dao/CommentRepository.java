package shop.matjalalzz.comment.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPartyId(Long partyId);

    List<Comment> findAllByInquiryId(Long inquiry);
}
