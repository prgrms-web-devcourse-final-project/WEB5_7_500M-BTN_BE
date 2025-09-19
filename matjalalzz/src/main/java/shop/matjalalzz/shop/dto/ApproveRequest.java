package shop.matjalalzz.shop.dto;

import jakarta.validation.constraints.NotNull;
import shop.matjalalzz.shop.entity.Approve;

public record ApproveRequest (
    @NotNull(message = "승인 여부를 입력하세요 APPROVED 또는 REJECTED")
    Approve approve
) {}
