package shop.matjalalzz.global.s3.dto;

import lombok.Builder;

@Builder
public record PreSignedUrlResponse(
    String key,
    String url
){

}
