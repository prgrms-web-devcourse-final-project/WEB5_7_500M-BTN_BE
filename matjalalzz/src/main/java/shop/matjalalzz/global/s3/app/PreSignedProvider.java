package shop.matjalalzz.global.s3.app;


import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.s3.dto.PreSignedItem;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import software.amazon.awssdk.services.s3.S3Client;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class PreSignedProvider  {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;


    @Value("${aws.s3.bucket}")
    private String bucketName;

    // URL 반환
    public URL getPresignedURL(String folderName, String userName, String standard, String subpath1) {
        String key = createPath(folderName, userName, standard, subpath1);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .cacheControl("no-cache, no-store, must-revalidate")
            //.acl("public-read")
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .putObjectRequest(objectRequest)
            .signatureDuration(Duration.ofMinutes(3))
            .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url();
    }

    // 이미지 삭제
    public void deleteImg(List<String> keyList) {
        List<ObjectIdentifier> objects = keyList.stream()
            .map(key -> ObjectIdentifier.builder().key(key).build())
            .collect(Collectors.toList());

        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
            .bucket(bucketName)
            .delete(Delete.builder().objects(objects).build())
            .build();

        s3Client.deleteObjects(deleteObjectsRequest);
    }

    public void deleteObjects(DeleteObjectsRequest deleteObjectsRequest) {
        s3Client.deleteObjects(deleteObjectsRequest);
    }



    // 반환되는 프리사이드 url
    public PreSignedUrlResponse generateShopPresignedUrl(String name, int imageCount, Long id) {
        List<PreSignedItem> items = new ArrayList<>();

        for (int i = 0; i < imageCount; i++) {
            String subPath = "img_" + i;
            URL url = getPresignedURL("shops", name, "SHOP_IMG", subPath);
            String key = String.format("shops/%s/%s_%s", name, name, subPath);
            items.add(new PreSignedItem(key, url.toString()));
        }

        return new PreSignedUrlResponse(items, id);
    }



    // 이미지 경로 생성
    private String createPath(String prefix, String userName, String standard, String subpath1) {

        if ("SHOP_IMG".equals(standard)) {
            return String.format("%s/%s/%s", prefix, userName, userName + "_" + subpath1);
        }
        // 프로필 이미지의 경우 여기서 경로 새롭게 생성 후 사용하면 될 듯

        return String.format("%s/%s", prefix, userName); // fallback
    }

}