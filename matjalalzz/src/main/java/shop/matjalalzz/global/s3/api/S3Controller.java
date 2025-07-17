//package shop.matjalalzz.global.s3.api;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestController;
//import shop.matjalalzz.global.s3.app.PreSignedService;
//import shop.matjalalzz.global.s3.dto.PreSignedCompliedRequest;
//import shop.matjalalzz.global.s3.dto.PreSignedCompliedReviewRequest;
//import shop.matjalalzz.global.security.PrincipalUser;
//
//@RestController
//@RequiredArgsConstructor
//public class S3Controller {
//
//    private final PreSignedService preSignedService;
//
//    @ResponseStatus(HttpStatus.CREATED)
//    @PostMapping("/upload-result")
//    public void presignedCompleted(@RequestBody PreSignedCompliedRequest request,
//        @AuthenticationPrincipal PrincipalUser principal) {
//        preSignedService.imageCompletion(request);
//    }
//
//    @ResponseStatus(HttpStatus.CREATED)
//    @PostMapping("/reviews/upload-result")
//    public void presignedCompletedReview(@RequestBody PreSignedCompliedReviewRequest request,
//        @AuthenticationPrincipal PrincipalUser principal) {
//        preSignedService.imageCompletion(request);
//    }
//
//}
