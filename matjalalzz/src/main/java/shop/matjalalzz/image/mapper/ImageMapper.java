package shop.matjalalzz.image.mapper;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import shop.matjalalzz.global.s3.dto.PreSignedCompledRequest;
import shop.matjalalzz.image.entity.Image;



@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageMapper {

    public static List<Image> toimagesList (PreSignedCompledRequest request) {
        int size = request.preSignedCompledItemList().size();
        List<Image> images =new ArrayList<>();
        //false가 없이 전부 다 성공이면 image db에 저장
        for (int i = 0; i < size; i++) {
            Image image = Image.builder()
                .imageIndex(i)
                .s3Key(request.preSignedCompledItemList().get(i).key())
                .completed(true)
                .shopId(request.shopId())
                .build();
            images.add(image);
        }
        return images;
    }



}
