package shop.matjalalzz.inquiry.message;


import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.global.discord.message.DiscordEmbeddable;
import shop.matjalalzz.user.entity.User;

@RequiredArgsConstructor
public class InquiryMessage implements DiscordEmbeddable {
    private final User user;
    private final String title;
    private final String content;



    @Override
    public String getTitle() {
        return "고객센터 문의 내용";
    }

//    @Override
//    public String getDescription() {
//        return "문의 사항이 접수 되었습니다.";
//    }

    @Override
    public Map<String, String> getFields() {
        Map<String, String> fields = new LinkedHashMap<>();

        String userInfo = """
        **사용자 이름:** %s
        **사용자 닉네임:** %s
        
        """.formatted(user.getName(), user.getNickname());
        fields.put("• 문의자 정보", userInfo);
        fields.put("\u200B", "\u200B");
        fields.put("• 문의 제목", title);
        fields.put("• 문의 내용", "```" + content + "```");

        // 해당 게시물로 가는 링크 넣어도 괜찮을 듯

        return fields;
    }
}
