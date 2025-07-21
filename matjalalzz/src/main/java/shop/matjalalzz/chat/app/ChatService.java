package shop.matjalalzz.chat.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.chat.dao.ChatMessageRepository;
import shop.matjalalzz.chat.dto.ChatMessageDto;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessageDto save(ChatMessageDto chatMessage) {
        return ChatMessageDto.fromEntity(chatMessageRepository.save(chatMessage.toEntity()));
    }

//    @Transactional(readOnly = true)
//    public List<ChatMessageDto> loadMessages(ChatLoadRequest request) {
//        return chatMessageRepository.findAllByIdGreaterThanAndRoomIdOrderById(
//                request.lastMessageId(), request.roomId())
//            .stream()
//            .map(ChatMessageDto::fromEntity)
//            .toList();
//    }
}
