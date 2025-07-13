//package shop.matjalalzz.global.s3.config;
//
//import java.net.URL;
//import java.util.List;
//import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
//
//public interface myS3Client {
//
//    /**
//     * Presigned URL 생성
//     */
//    URL getPresignedURL(String folderName, String userName, String standard, String subpath1);
//
//    /**
//     * S3 이미지 삭제
//     */
//    void deleteImg(List<String> keyList);
//
//    void deleteObjects(DeleteObjectsRequest deleteObjectsRequest);
//}
