package shop.matjalalzz.party.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import shop.matjalalzz.party.entity.enums.GenderCondition;
import shop.matjalalzz.party.entity.enums.PartyStatus;
import shop.matjalalzz.shop.entity.FoodCategory;

public record PartySearchCondition(
    @Schema(description = "파티 상태 (RECRUITING, COMPLETED, TERMINATED") PartyStatus status,
    @Schema(description = "성별 조건 ( M(남자), W(여자), A(무관) ") GenderCondition gender,
//    Boolean ageFilter,
    @Schema(description = "최소 나이") Integer minAge,
    @Schema(description = "최대 나이") Integer maxAge,
    @Schema(description = "시/도 단위 파티 위치") String location,
    //todo: 음식 카테고리를 여러 개 선택한다면?
    @Schema(description = " CHICKEN(치킨), CHINESE(중식), JAPANESE(일식), PIZZA(피자), FASTFOOD(패스트푸드), STEW_SOUP(찜/탕), JOK_BO(족발/보쌈), KOREAN(한식), SNACK(분식), WESTERN(양식), DESSERT(카페/디저트) 중 선") FoodCategory category,
    @Schema(description = "검색어 (파티 제목에 포함된 키워드)") String query,
    @Schema(description = "커서 ID (마지막 파티 ID)") Long cursor
) {

}
