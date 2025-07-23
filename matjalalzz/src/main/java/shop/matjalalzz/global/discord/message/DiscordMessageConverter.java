package shop.matjalalzz.global.discord.message;


import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordMessageConverter {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static MessageEmbed buildReportMessage(DiscordEmbeddable message) {
        String reportTime = TIME_FORMATTER.format(Instant.now().atZone(ZONE_ID));

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(message.getTitle())
            //.setDescription(message.getDescription())
            .addField("\u200B", "\u200B", false); // 줄바꿈

        for(Map.Entry<String, String> field : message.getFields().entrySet() ) {
            builder.addField(field.getKey(), field.getValue(), false);
        }

        // 공통적으로 들어가는 값 builder
         builder
             .addField("\u200B", "\u200B", false)
             .addField("접수 시각", "`" + reportTime + " (KST)`", false)
             .addField("\u200B", "\u200B", false)
             .setFooter("500M_BTN 관리 시스템")
             .setTimestamp(Instant.now())
             .setColor(0xFF6B6B);
        return builder.build();
    }



}
