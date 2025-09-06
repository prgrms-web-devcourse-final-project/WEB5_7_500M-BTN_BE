package shop.matjalalzz.image.app.query;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.dto.projection.ReviewImageProjection;
import shop.matjalalzz.image.entity.Image;

@Service
@RequiredArgsConstructor
public class ImageQueryService {

    private final ImageRepository imageRepository;

    @Transactional(readOnly = true)
    public List<String> findByInquiryImage(long inquiryId) {
        return imageRepository.findByInquiryImage(inquiryId);
    }

    @Transactional(readOnly = true)
    public Optional<Image> findShopThumbnail(Long shopId) {
        return imageRepository.findByShopIdAndImageIndex(shopId, 0);
    }

    @Transactional(readOnly = true)
    public List<ReviewImageProjection> findReviewImagesById(List<Long> reviewIds) {
        return imageRepository.findImageKeyByReviewIds(reviewIds);
    }

    @Transactional(readOnly = true)
    public List<Image> findByShopAndImage(long shopId) {
        return imageRepository.findByShopIdOrderByImageIndexAsc(shopId);
    }

    @Transactional(readOnly = true)
    public List<String> findByShopAndImageKey(long shopId) {
        return imageRepository.findByShopImageKey(shopId);
    }

    @Transactional(readOnly = true)
    public List<Image> findShopThumbnails(List<Long> shopIds) {
        return imageRepository.findThumbnailByShopIds(shopIds);
    }
}
