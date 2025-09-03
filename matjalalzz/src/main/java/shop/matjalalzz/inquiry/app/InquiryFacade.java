package shop.matjalalzz.inquiry.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.comment.dao.CommentRepository;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.image.app.ImageFacade;
import shop.matjalalzz.inquiry.app.command.InquiryCommandService;
import shop.matjalalzz.inquiry.app.query.InquiryQueryService;
import shop.matjalalzz.inquiry.dto.InquiryAllGetResponse;
import shop.matjalalzz.inquiry.dto.InquiryCreateRequest;
import shop.matjalalzz.inquiry.dto.InquiryItem;
import shop.matjalalzz.inquiry.dto.InquiryOneGetResponse;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.inquiry.mapper.InquiryMapper;
import shop.matjalalzz.user.app.UserService;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@Service
@RequiredArgsConstructor
public class InquiryFacade {
    private final InquiryCommandService inquiryCommandService;
    private final InquiryQueryService inquiryQueryService;
    private final PreSignedProvider preSignedProvider;
    private final ImageFacade imageFacade;
    private final CommentRepository commentRepository;
    private final UserService userService;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    // 문의글 생성
    @Transactional
    public PreSignedUrlListResponse createNewInquiry(long userId, InquiryCreateRequest request){
        User user = userService.getUserById(userId);
        Inquiry inquiry = InquiryMapper.toInquiry(request, user);
        inquiryCommandService.createNewInquiry(inquiry);
        return preSignedProvider.createInquiryUploadUrls(request.imageCount(), inquiry.getId());
    }


    // 문의글 전체 조회
    public InquiryAllGetResponse getAllInquiry(Long cursor, int size){
        Slice<Inquiry> inquirySlice = inquiryQueryService.getAllInquiry(cursor, size);
        Long nextCursor = null;
        if (inquirySlice.hasNext()){
            nextCursor = inquirySlice.getContent().getLast().getId();
        }
        List<InquiryItem> inquiryItems =  inquirySlice.stream().map(inquiry ->
        {
            // 댓글 갯수 조회도 commentQueryService에서 가져오는 형태로 바꿔야 함
            int answerCount = commentRepository.findAllByInquiryId(inquiry.getId()).size();
            return InquiryMapper.fromInquiryItem(inquiry, answerCount);
        }).toList();

        return new InquiryAllGetResponse(inquiryItems, nextCursor);
    }


    // 본인이 작성한 하나의 문의글 조회 (관리자도 조회 가능)
    public InquiryOneGetResponse getOneInquiry(long userId, Long inquiryId) {
        // 이것도 queryService 필요
        User user = userService.getUserById(userId);
        Inquiry inquiry = inquiryQueryService.getOneInquiry(inquiryId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_INQUIRY));

        //관리자도 아니고 자신이 쓴 문의글이 아니면 조회 불가
        if (!user.getRole().equals(Role.ADMIN) && !inquiry.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        List<String> imagesUrl = imageFacade.findByInquiryImage(inquiry.getId());
        List<String> imagesPathUrl = imagesUrl.stream().map(path-> BASE_URL  + path ).toList();
        return InquiryMapper.fromInquiry(inquiry, imagesPathUrl);


    }




}
