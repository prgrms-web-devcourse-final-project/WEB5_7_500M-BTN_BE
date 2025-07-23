package shop.matjalalzz.global.discord.api;


import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shop.matjalalzz.global.discord.message.DiscordMessageConverter;
import shop.matjalalzz.inquiry.message.InquiryMessage;
import shop.matjalalzz.shop.message.ShopOwnerMessage;
import shop.matjalalzz.user.entity.User;

@Service
@RequiredArgsConstructor
public class DiscordService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordService.class);

    private final JDA jda;

    @Value("${discord.bot.channel}")
    private String channelId;


    // 사용자 문의 요청
    public void InquirySendMessageToDiscord(User user, String title, String content ) {
        logger.info("Inquiry Send Message To Discord");
        TextChannel channel = jda.getTextChannelById(channelId);
        MessageEmbed buildEmbedReportMessage = DiscordMessageConverter.buildReportMessage(new InquiryMessage(user, title, content));
        channel.sendMessageEmbeds(buildEmbedReportMessage).queue();
    }

    //식당 사장의 식당 등록 요청
    public void ShopOwnerSendMessageToDiscord(User user, String shopName, String roadAddress, String deTailAddress) {
        logger.info("Shop Owner Send Message To Discord");
        TextChannel channel = jda.getTextChannelById(channelId);
        MessageEmbed buildEmbedReportMessage = DiscordMessageConverter.buildReportMessage(new ShopOwnerMessage(user, shopName,roadAddress, deTailAddress));
        channel.sendMessageEmbeds(buildEmbedReportMessage).queue();
    }





}
