package shop.matjalalzz.image.app;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shop.matjalalzz.image.app.command.ImageCommandService;
import shop.matjalalzz.image.app.query.ImageQueryService;
import shop.matjalalzz.image.dto.projection.ReviewImageProjection;
import shop.matjalalzz.image.entity.Image;

@Service
@RequiredArgsConstructor
public class ImageFacade {
    private final ImageQueryService imageQueryService;
    private final ImageCommandService imageCommandService;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;


    public List<String> findByInquiryImage(long inquiryId) {
        return imageQueryService.findByInquiryImage(inquiryId);
    }

    // 해당 식당에 thumbnail
    public String findByShopThumbnail(long shopId) {
        Optional<Image> optionalImage = imageQueryService.findShopThumbnail(shopId);
       return optionalImage.map(image -> BASE_URL + image.getS3Key()).orElse(null);
    }

    public Map<Long, List<String>> findReviewImagesById(List<Long> reviewIds){
        return imageQueryService.findReviewImagesById(reviewIds).stream().collect(
            Collectors.groupingBy(ReviewImageProjection::getReviewId,
                Collectors.mapping(v -> BASE_URL + v.getS3Key(), Collectors.toList())
            ));
    }


    public Map<Long, String> findByShopThumbnails(List<Long> shopIds) {
        return imageQueryService.findShopThumbnails(shopIds).stream()
            .collect(Collectors.toMap(
                Image::getShopId,
                image -> BASE_URL + image.getS3Key(),
                (exist, dup) -> exist // 중복 키 발생 시 기존값 유지
            ));
    }

}
