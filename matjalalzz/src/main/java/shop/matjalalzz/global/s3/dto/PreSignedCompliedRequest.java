package shop.matjalalzz.global.s3.dto;


import java.util.List;

public record PreSignedCompliedRequest(
    List<PreSignedCompliedItem> preSignedCompliedItemList, long shopId){}
