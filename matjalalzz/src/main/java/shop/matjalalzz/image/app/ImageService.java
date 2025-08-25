package shop.matjalalzz.image.app;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.dto.ReviewImageView;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public String getShopThumbnail(Long shopId) {
        return imageRepository.findByShopIdAndImageIndex(shopId, 0)
            .map(image -> BASE_URL + image.getS3Key())
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> findReviewImagesById(List<Long> reviewIds) {
        return imageRepository.findImageKeyByReviewIds(reviewIds).stream().collect(
            Collectors.groupingBy(ReviewImageView::getReviewId,
                Collectors.mapping(v -> BASE_URL + v.getS3Key(), Collectors.toList())
            ));
    }
}
