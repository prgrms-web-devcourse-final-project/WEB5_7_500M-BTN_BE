package shop.matjalalzz.image.app;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.entity.Image;

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
    public Map<Long, String> getShopThumbnails(List<Long> shopIds) {
        return imageRepository.findThumbnailByShopIds(shopIds).stream()
            .collect(Collectors.toMap(
                Image::getShopId,
                image -> BASE_URL + image.getS3Key(),
                (exist, dup) -> exist // 중복 키 발생 시 기존값 유지
            ));
    }
}
