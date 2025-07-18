package shop.matjalalzz.party.dto;

import java.util.List;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.shop.entity.FoodCategory;

public record PartySearchParam(
    PartyStatus status,
    GenderCondition gender,
//    Boolean ageFilter,
    Integer minAge,
    Integer maxAge,
    String location,
    List<FoodCategory> categories,
    String query,
    Long cursor
) {

}
