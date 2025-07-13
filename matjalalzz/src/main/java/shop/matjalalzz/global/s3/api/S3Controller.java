package shop.matjalalzz.global.s3.api;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedCompledRequest;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final PreSignedProvider preSignedProvider;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/presigned-urls")
    public void presignedCompleted(@RequestBody PreSignedCompledRequest request) {
        preSignedProvider.imageCompletion(request);
    }
}
