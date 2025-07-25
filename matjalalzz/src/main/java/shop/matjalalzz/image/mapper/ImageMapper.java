package shop.matjalalzz.image.mapper;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.global.s3.dto.PreSignedCompliedRequest;
import shop.matjalalzz.global.s3.dto.PreSignedCompliedReviewRequest;
import shop.matjalalzz.image.entity.Image;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageMapper {

    public static List<Image> toimagesList(PreSignedCompliedRequest request) {
        int size = request.preSignedCompliedItemList().size();
        List<Image> images = new ArrayList<>();
        //false가 없이 전부 다 성공이면 image db에 저장
        for (int i = 0; i < size; i++) {
            Image image = Image.builder()
                .imageIndex(i)
                .s3Key(request.preSignedCompliedItemList().get(i).key())
                //.completed(true)
                .shopId(request.shopId())
                .build();
            images.add(image);
        }
        return images;
    }

    public static List<Image> toimagesList(PreSignedCompliedReviewRequest request) {
        int size = request.preSignedCompliedItemList().size();
        List<Image> images = new ArrayList<>();
        //false가 없이 전부 다 성공이면 image db에 저장
        for (int i = 0; i < size; i++) {
            Image image = Image.builder()
                .imageIndex(i)
                .s3Key(request.preSignedCompliedItemList().get(i).key())
                //.completed(true)
                .reviewId(request.reviewId())
                .build();
            images.add(image);
        }
        return images;
    }


    public static Image UrlResponseToShopImage(String s3Key, int i, long shopId) {
        return Image.builder()
            .s3Key(s3Key)
            .imageIndex(i)
            .shopId(shopId)
            .build();
    }

    public static Image UrlResponseToReviewImage(String s3Key, int i, long reviewId) {
        return Image.builder()
            .s3Key(s3Key)
            .imageIndex(i)
            .reviewId(reviewId)
            .build();
    }

    public static Image UrlResponseToInquiryImage(String s3Key, int i, long inquiryId) {
        return Image.builder()
            .s3Key(s3Key)
            .imageIndex(i)
            .inquiryId(inquiryId)
            .build();
    }


}
