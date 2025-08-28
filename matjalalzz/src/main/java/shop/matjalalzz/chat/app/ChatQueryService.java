package shop.matjalalzz.chat.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import shop.matjalalzz.chat.dao.ChatMessageRepository;
import shop.matjalalzz.chat.entity.ChatMessage;

@Service
@RequiredArgsConstructor
public class ChatQueryService {

    private final ChatMessageRepository chatMessageRepository;

    public Slice<ChatMessage> findChatByPartyId(Long partyId, Long cursor) {
        return chatMessageRepository.findByPartyIdAndCursor(
            partyId, cursor, PageRequest.of(0, 20));
    }

    public List<ChatMessage> findLatestChatByPartyId(Long partyId) {
        return chatMessageRepository.findAllByPartyIdOrderByIdDesc(partyId,
            PageRequest.of(0, 30));
    }
}
