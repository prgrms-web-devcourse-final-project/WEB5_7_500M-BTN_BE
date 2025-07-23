package shop.matjalalzz.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record InquiryCreateRequest(

    @Schema(description = "문의 제목", example = "포인트 환급 관련 문의 또는 다른 문의")
    @NotBlank(message = "문의 사항 제목을 입력해 주세요.")
    String title,

    @Schema(description = "문의 내용", example = "포인트를 이 계좌로 환급해서 보내주세요.")
    @NotBlank(message = "문의 내용을 입력해 주세요")
    String content,

    @Schema(description = "넣으려는 사진 갯수", example = "3")
    @NotBlank(message = "몇장의 사진을 넣는지 입력하세요")
    int imageCount
)
{}