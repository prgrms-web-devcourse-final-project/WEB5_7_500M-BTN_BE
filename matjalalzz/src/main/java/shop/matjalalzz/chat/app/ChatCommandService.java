package shop.matjalalzz.chat.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.chat.dao.ChatMessageRepository;
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
public class ChatCommandService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final PartyService partyService;

    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long userId) {
        if (!partyService.isInParty(request.partyId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        User user = userService.getUserById(userId);
        Party party = partyService.findById(request.partyId());

        ChatMessage chatMessage = chatMessageRepository.save(
            ChatMapper.fromChatMessageRequest(request, user, party));

        return ChatMapper.toChatMessageResponse(chatMessage);
    }
}
