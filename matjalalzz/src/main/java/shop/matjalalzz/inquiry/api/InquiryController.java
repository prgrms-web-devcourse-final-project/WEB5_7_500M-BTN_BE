package shop.matjalalzz.inquiry.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.matjalalzz.global.common.BaseResponse;
import shop.matjalalzz.global.common.BaseStatus;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.global.security.PrincipalUser;
import shop.matjalalzz.inquiry.app.InquiryService;
import shop.matjalalzz.inquiry.dto.InquiryAllGetResponse;
import shop.matjalalzz.inquiry.dto.InquiryCreateRequest;
import shop.matjalalzz.inquiry.dto.InquiryOneGetResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "고객센터 API", description = "고객센터 관련 API")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation( summary = "고객센터의 문의글 작성", description = """
    제목과 내용을 작성하여 문의글을 작성합니다
    
    사진 전송 시 헤더에
    Cache-Control 값이 no-cache,no-store,must-revalidate 되어 있어야 합니다
   
   (Completed)
    """)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/inquiry")
    public BaseResponse<PreSignedUrlListResponse> newInquiry(@AuthenticationPrincipal PrincipalUser principal,
        @RequestBody @Valid InquiryCreateRequest request) {
        return BaseResponse.ok(inquiryService.newInquiry(principal.getId(), request), BaseStatus.OK);
    }


    @Operation( summary = "고객센터의 문의글 전체 조회", description = "문의글 전체를 조회합니다. (Completed)")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/inquiry")
    public BaseResponse<InquiryAllGetResponse> getAllInquiry(
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "10", required = false) int size) {
        return BaseResponse.ok(inquiryService.getAllInquiry(cursor,size), BaseStatus.OK);
    }



    @Operation( summary = "자신이 작성한 고객센터의 문의글 하나 상세 조회", description = "본인이 작성한 경우이거나 관리자의 경우에만 조회가 가능합니다. (Completed)")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/inquiry/{inquiryId}")
    public BaseResponse<InquiryOneGetResponse> getOneInquiry(@AuthenticationPrincipal PrincipalUser principal,
        @PathVariable Long inquiryId) {
        return BaseResponse.ok(inquiryService.getOneInquiry(principal.getId(),  inquiryId), BaseStatus.OK);
    }


}
