package shop.matjalalzz.chat.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ChatMessagePageResponse(
    Long nextCursor,
    List<ChatMessageResponse> content

) {

}
