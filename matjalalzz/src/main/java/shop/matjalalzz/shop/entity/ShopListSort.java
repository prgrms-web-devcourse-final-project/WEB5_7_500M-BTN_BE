package shop.matjalalzz.shop.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShopListSort {
    RATING("rating"),
    CREATED_AT("createdAt"),
    NAME("name");

    @JsonValue
    private final String name;
}
