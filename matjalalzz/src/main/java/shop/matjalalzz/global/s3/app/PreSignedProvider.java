package shop.matjalalzz.global.s3.app;


import jakarta.transaction.Transactional;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.dto.PreSignedCompledItem;
import shop.matjalalzz.global.s3.dto.PreSignedCompledRequest;
import shop.matjalalzz.global.s3.dto.PreSignedItem;
import shop.matjalalzz.global.s3.dto.PreSignedUrlResponse;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.entity.Image;
import shop.matjalalzz.shop.dao.ShopRepository;
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
    private final ImageRepository imageRepository;
    private final ShopRepository shopRepository;

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

    //TODO 이거 서비스 코드로 빼야 함
    // 프리사이드 url에 이미지가 성공적으로 저장 되었는지 확인 후 image db에 저장
    @Transactional
    // 발급 된 프리사이드 url에 이미지가 확실하게 저장되면 image DB에 저장, 여러 이미지 중 하나라도 false가 있으면 s3에 있던 이미지 전체 삭제
    public void imageCompletion (PreSignedCompledRequest request){

        shopRepository.findById(request.shopId()).orElseThrow(()-> new  BusinessException((ErrorCode.NOT_FIND_SHOP)));

        int size = request.preSignedCompledItemList().size();

        List<String> allKeys = request.preSignedCompledItemList().stream().map(
            PreSignedCompledItem::key).toList();

        for (int i = 0; i < size; i++) {
                if (!request.preSignedCompledItemList().get(i).completion()){
                    //s3에 올라간 이미지들 전체 삭제 후 에러 반환
                    deleteImg(allKeys);
                    throw new BusinessException(ErrorCode.IMAGE_SAVE_FAILED);
                }
        }

        // TODO 실제로 s3 객체 사진이 실제로 존재하는지 확인 여부 필요

        //mapper로 빼기
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
        imageRepository.saveAll(images);

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