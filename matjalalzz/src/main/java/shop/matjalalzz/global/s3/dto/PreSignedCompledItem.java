package shop.matjalalzz.global.s3.dto;

public record PreSignedCompledItem (
    String key,
    boolean completion
) {}
