package shop.matjalalzz.party.dto;

import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.shop.entity.FoodCategory;

public record PartySearchCondition(
    PartyStatus status,
    GenderCondition gender,
    Boolean ageFilter,
    String location,
    FoodCategory category,
    String query,
    Long cursor
) {

}
