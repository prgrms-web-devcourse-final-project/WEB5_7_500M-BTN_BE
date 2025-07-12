package shop.matjalalzz.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import lombok.Builder;
import shop.matjalalzz.shop.entity.FoodCategory;

@Builder
public record ShopUpdateRequest(
    @Schema(description = "상점 이름")
    String name,

    @Schema(description = "도로명 주소")
    String roadAddress,

    @Schema(description = "상세 주소")
    String detailAddress,

    @Schema(description = "시/도")
    String sido,

    @Schema(description = "위도")
    Double latitude,

    @Schema(description = "경도")
    Double longitude,

    @Schema(description = "사업자 등록번호")
    String businessCode,

    @Schema(description = "음식 카테고리")
    FoodCategory category,

    @Schema(description = "예약 수수료")
    int reservationFee,

    @Schema(description = "영업 시작 시간")
    LocalTime openTime,

    @Schema(description = "영업 종료 시간")
    LocalTime closeTime,

    @Schema(description = "상점 설명")
    String description

    //TODO: Image 추가
) {

}
