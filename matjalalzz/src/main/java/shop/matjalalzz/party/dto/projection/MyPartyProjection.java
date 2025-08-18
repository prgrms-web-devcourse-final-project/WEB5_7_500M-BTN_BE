package shop.matjalalzz.party.dto.projection;

import java.time.LocalDateTime;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;

public interface MyPartyProjection {

    Long getId();

    String getTitle();

    String getShopName();

    LocalDateTime getMetAt();

    LocalDateTime getDeadline();

    PartyStatus getStatus();

    int getMaxCount();

    int getMinCount();

    int getCurrentCount();

    GenderCondition getGenderCondition();

    int getMinAge();

    int getMaxAge();

    String getDescription();

    boolean getIsHost();

}
