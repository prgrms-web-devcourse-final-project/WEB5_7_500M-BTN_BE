package shop.matjalalzz.global.discord.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfig {

    @Bean
    JDA jda(@Value("${discord.bot.token}") String botToken ) throws InterruptedException {
        return JDABuilder.createDefault(botToken)
            //메모리 사용량을 줄이기 위해 다 끔
            .disableCache(CacheFlag.VOICE_STATE)
            .disableCache(CacheFlag.EMOJI)
            .disableCache(CacheFlag.CLIENT_STATUS)
            .disableCache(CacheFlag.STICKER)
            .disableCache(CacheFlag.SCHEDULED_EVENTS)
            .setEnabledIntents( //봇이 수신 가능한 이벤트 명시
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
            )
            .build()
            .awaitReady(); //디스코드 서버와 봇이 연결 될 때까지 기다림

    }

}
