package shop.matjalalzz.chat.app;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.chat.dao.ChatMessageRepository;
import shop.matjalalzz.chat.dto.ChatMessageResponse;
import shop.matjalalzz.chat.entity.ChatMessage;
import shop.matjalalzz.chat.entity.MessageType;
import shop.matjalalzz.chat.mapper.ChatMapper;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class PartyChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatSubscriptionService subscriptionService;
    private final ChatMessageRepository chatMessageRepository;

    public void kickUser(User user, Party party) {
        subscriptionService.leaveParty(user, party);

        ChatMessageResponse kickMessage = ChatMessageResponse.builder()
            .type(MessageType.KICK)
            .partyId(party.getId())
            .userNickname(user.getNickname())
            .userId(user.getId())
            .build();

        messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/notice",
            kickMessage);
    }

    public void leaveParty(User user, Party party) {
        subscriptionService.leaveParty(user, party);

        ChatMessage chatMessage = ChatMessage.builder()
            .sender(user)
            .party(party)
            .type(MessageType.LEAVE)
            .build();
        ChatMessageResponse leaveMessage = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), leaveMessage);
        chatMessageRepository.save(chatMessage);
    }

    public void noticePaymentRequest(User user, Party party) {
        ChatMessage chatMessage = ChatMessage.builder()
            .party(party)
            .sender(user)
            .type(MessageType.REQUEST_PAYMENT)
            .build();
        ChatMessageResponse noticeMessage = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), noticeMessage);
        chatMessageRepository.save(chatMessage);
    }

    public void noticePaymentComplete(User user, Party party) {
        ChatMessage chatMessage = ChatMessage.builder()
            .type(MessageType.COMPLETE_PAYMENT)
            .party(party)
            .sender(user)
            .build();
        ChatMessageResponse noticeMessage = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), noticeMessage);
        chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public void join(Party party, User user) {
        ChatMessage chatMessage = ChatMessage.builder()
            .sender(user)
            .party(party)
            .type(MessageType.JOIN)
            .build();

        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), chatMessage);
        chatMessageRepository.save(chatMessage);
    }

}
