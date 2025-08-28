package shop.matjalalzz.chat.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.matjalalzz.chat.dao.ChatMessageRepository;
import shop.matjalalzz.chat.entity.ChatMessage;

@Service
@RequiredArgsConstructor
public class ChatCommandService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }
}
