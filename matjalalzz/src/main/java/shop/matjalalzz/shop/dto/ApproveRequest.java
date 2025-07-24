package shop.matjalalzz.shop.dto;

import jakarta.validation.constraints.NotBlank;
import shop.matjalalzz.shop.entity.Approve;

public record ApproveRequest (
    @NotBlank (message = "승인 여부를 입력하세요 APPROVED 또는 REJECTED")
    Approve approve
) {}
