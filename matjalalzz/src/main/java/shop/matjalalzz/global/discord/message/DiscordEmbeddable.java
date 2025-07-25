package shop.matjalalzz.global.discord.message;

import java.util.Map;

public interface DiscordEmbeddable {

    String getTitle();
    //String getDescription();
    Map<String, String> getFields();

}
