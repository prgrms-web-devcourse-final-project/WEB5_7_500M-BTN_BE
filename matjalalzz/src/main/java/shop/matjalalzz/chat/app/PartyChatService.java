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
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSubscriptionService chatSubscriptionService;


    public void kickUser(User kickedUser, Party party) {
        leaveParty(kickedUser, party);

        ChatMessageResponse kickMessage = ChatMessageResponse.builder()
            .type(MessageType.KICK)
            .partyId(party.getId())
            .userNickname(kickedUser.getNickname())
            .userId(kickedUser.getId())
            .build();

        messagingTemplate.convertAndSendToUser(kickedUser.getId().toString(), "/queue",
            kickMessage);
    }

    public void leaveParty(User leftUser, Party party) {
        chatSubscriptionService.unsubscribeParty(leftUser, party);

        ChatMessage chatMessage = ChatMessage.builder()
            .sender(leftUser)
            .party(party)
            .type(MessageType.LEAVE)
            .build();
        ChatMessageResponse leaveMessage = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), leaveMessage);
        chatMessageRepository.save(chatMessage);
    }

    public void noticePaymentRequest(User host, Party party) {
        ChatMessage chatMessage = ChatMessage.builder()
            .party(party)
            .sender(host)
            .type(MessageType.PAYMENT_REQUEST)
            .build();
        ChatMessageResponse noticeMessage = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), noticeMessage);
        chatMessageRepository.save(chatMessage);
    }

    public void noticePaymentComplete(User payer, Party party) {
        ChatMessage chatMessage = ChatMessage.builder()
            .type(MessageType.PAYMENT_COMPLETE)
            .party(party)
            .sender(payer)
            .build();
        ChatMessageResponse noticeMessage = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), noticeMessage);
        chatMessageRepository.save(chatMessage);
    }

    public void noticeReservationComplete(User host, Party party) {
        ChatMessage chatMessage = ChatMessage.builder()
            .type(MessageType.RESERVATION_COMPLETE)
            .party(party)
            .sender(host)
            .build();
        ChatMessageResponse noticeMessage = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), noticeMessage);
        chatMessageRepository.save(chatMessage);
    }

    public void noticePartyDeleted(User host, Party party) {
        ChatMessage chatMessage = ChatMessage.builder()
            .type(MessageType.PARTY_DELETED)
            .party(party)
            .sender(host)
            .build();
        ChatMessageResponse noticeMessage = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), noticeMessage);
        chatMessageRepository.save(chatMessage);

        party.getPartyUsers().forEach(pu -> {
            chatSubscriptionService.unsubscribeParty(pu.getUser(), party);
        });
    }

    @Transactional
    public void join(User joinedUser, Party party) {
        ChatMessage chatMessage = ChatMessage.builder()
            .sender(joinedUser)
            .party(party)
            .type(MessageType.JOIN)
            .build();

        ChatMessageResponse response = ChatMapper.toChatMessageResponse(chatMessage);
        messagingTemplate.convertAndSend("/topic/party/" + party.getId(), response);
        chatMessageRepository.save(chatMessage);
    }

}
