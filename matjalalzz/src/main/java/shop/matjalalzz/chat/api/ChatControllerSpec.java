package shop.matjalalzz.chat.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import shop.matjalalzz.chat.dto.ChatMessagePageResponse;
import shop.matjalalzz.chat.dto.ChatMessageResponse;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.security.PrincipalUser;

@Tag(name = "채팅 API", description = "채팅 관련 API")
public interface ChatControllerSpec {

    @GetMapping("/parties/{partyId}/chat/restore")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    BaseResponse<List<ChatMessageResponse>> restoreChat(Long partyId, PrincipalUser user);

    @GetMapping("/parties/{partyId}/chat/load")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    BaseResponse<ChatMessagePageResponse> loadChatHistory(Long partyId, Long cursor,
        PrincipalUser user);
}
