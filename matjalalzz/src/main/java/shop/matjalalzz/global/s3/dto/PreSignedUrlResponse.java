package shop.matjalalzz.global.s3.dto;

import java.util.List;


public record PreSignedUrlResponse (List<PreSignedItem> preSignedResponse, long shopId)
{}