package shop.matjalalzz.chat.app;

import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import shop.matjalalzz.chat.dao.ChatMessageRepository;
import shop.matjalalzz.chat.dto.ChatMessageResponse;
import shop.matjalalzz.chat.entity.ChatMessage;
import shop.matjalalzz.chat.entity.MessageType;
import shop.matjalalzz.chat.mapper.ChatMapper;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.entity.User;

@Service
public class ChatSubscriptionService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;
    private final MessageChannel messageChannel;
    private final ChatMessageRepository chatMessageRepository;

    public ChatSubscriptionService(
        SimpMessagingTemplate messagingTemplate,
        SimpUserRegistry userRegistry,
        @Qualifier("clientInboundChannel") MessageChannel messageChannel,
        ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
        this.messageChannel = messageChannel;
        this.chatMessageRepository = chatMessageRepository;
    }

    public void kickUser(User user, Party party) {
        leaveParty(user, party);

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
        SimpUser simpUser = userRegistry.getUser(user.getId().toString());
        if (simpUser != null) {
            String destination = "/topic/party/" + party.getId();
            simpUser.getSessions().forEach(session -> {
                Set<SimpSubscription> subscriptions = session.getSubscriptions();
                subscriptions.stream().filter(s -> s.getDestination().equals(destination))
                    .forEach(s -> {
                        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(
                            StompCommand.UNSUBSCRIBE);
                        headerAccessor.setSessionId(session.getId());
                        headerAccessor.setSubscriptionId(s.getId());
                        headerAccessor.setDestination(s.getDestination());
                        headerAccessor.setUser(simpUser.getPrincipal());
                        headerAccessor.setMessageTypeIfNotSet(SimpMessageType.UNSUBSCRIBE);

                        messageChannel.send(MessageBuilder.createMessage(new byte[0],
                            headerAccessor.getMessageHeaders()));
                    });
            });
            ChatMessage chatMessage = ChatMessage.builder()
                .sender(user)
                .party(party)
                .type(MessageType.LEAVE)
                .build();
            ChatMessageResponse leaveMessage = ChatMapper.toChatMessageResponse(chatMessage);
            messagingTemplate.convertAndSend("/topic/party/" + party.getId(), leaveMessage);
            chatMessageRepository.save(chatMessage);
        }
    }
}
