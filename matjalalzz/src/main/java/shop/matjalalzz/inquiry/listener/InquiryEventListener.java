package shop.matjalalzz.inquiry.listener;


import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.discord.api.DiscordService;
import shop.matjalalzz.inquiry.entity.Inquiry;

@Component
@RequiredArgsConstructor
public class InquiryEventListener {

    private final DiscordService discordService;

    @Description("고객센터 문의가 접수 될 시 Discord 알람을 보낸다")
    @Async
    @EventListener(Inquiry.class)
    public void onInquiryAddEvent(Inquiry event){
        discordService.InquirySendMessageToDiscord(
            event.getUser(),
            event.getTitle(),
            event.getContent()
        );

    }



}
