package shop.matjalalzz.global.s3.dto;

public record PreSignedCompliedItem(
    String key,
    boolean completion
) {}
