package shop.matjalalzz.global.s3.app;


import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.image.entity.enums.ImageType;
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

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.exp-min}")
    private int expMin;

    public PreSignedUrlListResponse createShopUploadUrls(int count, long shopId) {
        List<PreSignedUrlResponse> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            items.add(buildItem(ImageType.SHOP_IMG, shopId, "img_" + i));
        }

        return new PreSignedUrlListResponse(items, shopId);
    }

    public PreSignedUrlResponse createProfileUploadUrls(long userId) {
        return buildItem(ImageType.PROFILE_IMG, userId, "img");
    }

    // 한 개 삭제
    public void deleteObject(String key) {
        deleteObjects(List.of(key));
    }

    // 여러 개 삭제
    public void deleteObjects(List<String> keys) {
        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
            .bucket(bucketName)
            .delete(
                Delete.builder()
                    .objects(
                        keys.stream().map(
                            k -> ObjectIdentifier.builder().key(k).build()
                        ).toList()
                    )
                    .build()
            )
            .build();

        s3Client.deleteObjects(request);
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