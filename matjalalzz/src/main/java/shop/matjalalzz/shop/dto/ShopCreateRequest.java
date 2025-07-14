package shop.matjalalzz.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopCreateRequest(
    @Schema(description = "상점 이름")
    @NotBlank
    String shopName,

    @Schema(description = "도로명 주소")
    @NotBlank
    String roadAddress,

    @Schema(description = "상세 주소")
    @NotBlank
    String detailAddress,

    @Schema(description = "시/도")
    @NotBlank
    String sido,

    @Schema(description = "위도")
    @NotNull
    Double latitude,

    @Schema(description = "경도")
    @NotNull
    Double longitude,

    @Schema(description = "사업자 등록번호")
    @NotBlank
    String businessCode,

    @Schema(description =
    """
    음식 카테고리 (아래 값 중 하나를 입력하세요):
    - CHICKEN: 치킨
    - CHINESE: 중식
    - JAPANESE: 일식
    - PIZZA: 피자
    - FASTFOOD: 패스트푸드
    - STEW_SOUP: 찜/탕
    - JOK_BO: 족발/보쌈
    - KOREAN: 한식
    - SNACK: 분식
    - WESTERN: 양식
    - DESSERT: 카페/디저트
    """)
    @NotNull
    FoodCategory category,

    @Schema(description = "예약 수수료")
    @NotNull
    int reservationFee,

    @Schema(description = "영업 시작 시간")
    LocalTime openTime,

    @Schema(description = "영업 종료 시간")
    LocalTime closeTime,

    @Schema(description = "상점 설명")
    @NotBlank
    String description,

    @Schema(description = "전화번호")
    @NotNull
    String tel,

    int imageCount

) {

}
