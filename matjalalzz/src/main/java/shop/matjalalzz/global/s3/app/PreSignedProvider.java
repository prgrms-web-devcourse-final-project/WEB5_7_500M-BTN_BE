package shop.matjalalzz.global.s3.app;


import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.image.entity.enums.ImageType;
import shop.matjalalzz.image.mapper.ImageMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RequiredArgsConstructor
@Component
public class PreSignedProvider {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final ImageRepository imageRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.exp-min}")
    private int expMin;

    @Transactional
    public PreSignedUrlListResponse createShopUploadUrls(int count, long shopId) {
        List<PreSignedUrlResponse> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            PreSignedUrlResponse preSignedUrlResponse = buildItem(ImageType.SHOP_IMG, shopId, "img_" + i);
            items.add(preSignedUrlResponse);

            String s3Key = preSignedUrlResponse.key();
            Image imageValue = ImageMapper.UrlResponseToImage(s3Key, i, shopId);
            imageRepository.save(imageValue);

        }

        return new PreSignedUrlListResponse(items, shopId);
    }

    @Transactional
    public PreSignedUrlListResponse createReviewUploadUrls(int count, long shopId, long reviewId) {
        List<PreSignedUrlResponse> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            PreSignedUrlResponse preSignedUrlResponse = buildItem(ImageType.SHOP_IMG, shopId, "img_review_" + reviewId + "_" + i);
            items.add(preSignedUrlResponse);
            String s3Key = preSignedUrlResponse.key();
            Image imageValue = ImageMapper.UrlResponseToImage(s3Key, i, shopId);
            imageRepository.save(imageValue);

        }

        return new PreSignedUrlListResponse(items, reviewId);
    }

    public PreSignedUrlResponse createProfileUploadUrls(long userId) {
        return buildItem(ImageType.PROFILE_IMG, userId, "img_" + UUID.randomUUID());
    }

    // 한 개 삭제
    public void deleteObject(String key) {
        if (key == null) {
            return;
        }

        deleteObjects(List.of(key));
    }

    // 여러 개 삭제
    public void deleteObjects(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
            .bucket(bucketName)
            .delete(
                Delete.builder()
                    .objects(
                        keys.stream()
                            .filter(Objects::nonNull)
                            .map(
                                k -> ObjectIdentifier.builder().key(k).build()
                            ).toList()
                    )
                    .build()
            )
            .build();
        s3Client.deleteObjects(request);
        keys.forEach(imageRepository::deleteByS3Key);
    }

    private PreSignedUrlResponse buildItem(ImageType type, long id, String subPath) {
        String key = buildKey(type, id, subPath);
        URL url = presignPutUrl(key);

        return new PreSignedUrlResponse(key, url.toString());
    }

    private String buildKey(ImageType type, long id, String subPath) {
        return "%s/%d/%d_%s".formatted(type.folder(), id, id, subPath);
    }

    private URL presignPutUrl(String key) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .cacheControl("no-cache,no-store,must-revalidate")
            .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
            .putObjectRequest(request)
            .signatureDuration(Duration.ofMinutes(expMin))
            .build();

        PresignedPutObjectRequest res = s3Presigner.presignPutObject(presignReq);
        return res.url();
    }

}