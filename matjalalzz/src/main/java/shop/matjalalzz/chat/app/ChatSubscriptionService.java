package shop.matjalalzz.chat.app;

import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import shop.matjalalzz.party.entity.Party;
import shop.matjalalzz.user.entity.User;

@Service
public class ChatSubscriptionService {

    private final SimpUserRegistry userRegistry;
    private final MessageChannel messageChannel;

    public ChatSubscriptionService(
        SimpUserRegistry userRegistry,
        @Qualifier("clientInboundChannel") MessageChannel messageChannel) {
        this.userRegistry = userRegistry;
        this.messageChannel = messageChannel;
    }

    public void unsubscribeParty(User user, Party party) {
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
        }
    }
}
