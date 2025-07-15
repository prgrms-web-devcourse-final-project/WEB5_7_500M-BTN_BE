package shop.matjalalzz.global.s3.dto;

import java.util.List;

public record PreSignedUrlListResponse(
    List<PreSignedUrlResponse> items,
    long refId
) {

}