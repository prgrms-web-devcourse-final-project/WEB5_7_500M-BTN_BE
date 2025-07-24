package shop.matjalalzz.shop.message;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.global.discord.message.DiscordEmbeddable;
import shop.matjalalzz.user.entity.User;

@RequiredArgsConstructor
public class ShopOwnerMessage implements DiscordEmbeddable {
    private final User user;

    private final String shopName;

    private final String roadAddress;

    private final String detailAddress;



    @Override
    public String getTitle() {
        return "식당 등록 신청 문의";
    }

//    @Override
//    public String getDescription() {
//        return "식당 등록이 접수 되었습니다.";
//    }

    @Override
    public Map<String, String> getFields() {
        Map<String, String> fields = new HashMap<>();


        fields.put("문의자 정보", """
            • 사용자 이름: %s
            • 사용자 닉네임: %s
            


            **상점 정보**
            • 상점 이름: %s
            • 상점 주소: %s
            • 상세 주소: %s
            
            """.formatted(
            user.getName(),
            user.getNickname(),
            shopName,
            roadAddress,
            detailAddress
        ));


        return fields;
        // 해당 게시물로 가는 링크 넣어도 괜찮을 듯
    }
}
