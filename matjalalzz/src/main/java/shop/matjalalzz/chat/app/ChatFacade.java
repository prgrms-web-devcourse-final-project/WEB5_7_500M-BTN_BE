package shop.matjalalzz.chat.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.chat.dto.ChatMessagePageResponse;
import shop.matjalalzz.chat.dto.ChatMessageRequest;
import shop.matjalalzz.chat.dto.ChatMessageResponse;
import shop.matjalalzz.chat.entity.ChatMessage;
import shop.matjalalzz.chat.mapper.ChatMapper;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.party.app.PartyService;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class ChatFacade {

    private final UserService userService;
    private final PartyService partyService;
    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long userId) {
        validateIsInParty(request.partyId(), userId);

        User user = userService.getUserById(userId);
        Party party = partyService.findById(request.partyId());

        ChatMessage chatMessage = chatCommandService.save(
            ChatMapper.fromChatMessageRequest(request, user, party));

        return ChatMapper.toChatMessageResponse(chatMessage);
    }

    @Transactional(readOnly = true)
    public ChatMessagePageResponse loadMessages(Long partyId, Long cursor, Long userId) {
        validateIsInParty(partyId, userId);
        Slice<ChatMessage> chatMessages = chatQueryService.findChatByPartyId(partyId, cursor);
        Long nextCursor = null;

        if (chatMessages.hasNext()) {
            nextCursor = chatMessages.getContent().getLast().getId();
        }

        return ChatMapper.toChatMessagePageResponse(chatMessages, nextCursor);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> restoreMessages(Long partyId, Long userId) {
        if (!partyService.isInParty(partyId, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return chatQueryService.findLatestChatByPartyId(partyId)
            .stream()
            .map(ChatMapper::toChatMessageResponse)
            .toList().reversed();
    }

    private void validateIsInParty(Long partyId, Long userId) {
        if (!partyService.isInParty(partyId, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

}
