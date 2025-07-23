package shop.matjalalzz.shop.listener;


import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.discord.api.DiscordService;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.shop.entity.Shop;

@Component
@RequiredArgsConstructor
public class ShopEventListener {

    private final DiscordService discordService;

    @Description("고객센터 문의가 접수 될 시 Discord 알람을 보낸다")
    @Async
    @EventListener(Shop.class)
    public void onShopAddEvent(Shop event){
        discordService.ShopOwnerSendMessageToDiscord(
            event.getUser(),
            event.getShopName(),
            event.getRoadAddress(),
            event.getDetailAddress()

        );

    }
}
