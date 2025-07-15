package shop.matjalalzz.global.s3.dto;


import java.util.List;

public record PreSignedCompliedReviewRequest(
    List<PreSignedCompliedItem> preSignedCompliedItemList, long reviewId) {

}
