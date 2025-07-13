package shop.matjalalzz.global.s3.dto;


import java.util.List;

public record PreSignedCompledRequest(
    List<PreSignedCompledItem> preSignedCompledItemList
    , long shopId
    ){}
