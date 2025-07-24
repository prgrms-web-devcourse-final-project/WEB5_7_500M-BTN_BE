package shop.matjalalzz.chat.dao;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.matjalalzz.chat.entity.ChatMessage;

public interface ChatMessageRepository extends
    JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByIdGreaterThanAndPartyIdOrderById(Long id,
        Long partyId);

    @Query("SELECT c FROM ChatMessage c WHERE c.party.id = :partyId AND (c.id < :cursor OR :cursor IS NULL ) ORDER BY c.id DESC")
    Slice<ChatMessage> findByPartyIdAndCursor(@Param("partyId") Long partyId,
        @Param("cursor") Long cursor,
        Pageable pageable);

}
