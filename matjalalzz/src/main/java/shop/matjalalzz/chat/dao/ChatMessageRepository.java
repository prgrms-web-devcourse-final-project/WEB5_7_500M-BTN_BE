package shop.matjalalzz.chat.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.matjalalzz.chat.entity.ChatMessage;

public interface ChatMessageRepository extends
    JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByIdGreaterThanAndPartyIdOrderById(Long id,
        Long partyId);

}
