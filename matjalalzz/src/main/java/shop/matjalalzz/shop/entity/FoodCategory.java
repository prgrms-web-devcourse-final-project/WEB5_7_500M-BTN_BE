package shop.matjalalzz.shop.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodCategory {
    CHICKEN("치킨"),
    CHINESE("중식"),
    JAPANESE("일식"),
    PIZZA("피자"),
    FASTFOOD("패스트푸드"),
    STEW_SOUP("찜/탕"),
    JOK_BO("족발/보쌈"),
    KOREAN("한식"),
    SNACK("분식"),
    WESTERN("양식"),
    DESSERT("카페/디저트");

    private final String koreanName;
}
