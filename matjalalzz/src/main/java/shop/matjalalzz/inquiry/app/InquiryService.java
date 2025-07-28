package shop.matjalalzz.inquiry.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.matjalalzz.comment.dao.CommentRepository;
import shop.matjalalzz.global.exception.BusinessException;
import shop.matjalalzz.global.exception.domain.ErrorCode;
import shop.matjalalzz.global.s3.app.PreSignedProvider;
import shop.matjalalzz.global.s3.dto.PreSignedUrlListResponse;
import shop.matjalalzz.image.dao.ImageRepository;
import shop.matjalalzz.image.mapper.ImageMapper;
import shop.matjalalzz.inquiry.dao.InquiryRepository;
import shop.matjalalzz.inquiry.dto.InquiryAllGetResponse;
import shop.matjalalzz.inquiry.dto.InquiryCreateRequest;
import shop.matjalalzz.inquiry.dto.InquiryItem;
import shop.matjalalzz.inquiry.dto.InquiryOneGetResponse;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.inquiry.mapper.InquiryMapper;
import shop.matjalalzz.user.dao.UserRepository;
import shop.matjalalzz.user.entity.User;
import shop.matjalalzz.user.entity.enums.Role;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final PreSignedProvider preSignedProvider;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;

    @Value("${aws.credentials.AWS_BASE_URL}")
    private String BASE_URL;

    @Transactional
    public PreSignedUrlListResponse newInquiry(long userId, InquiryCreateRequest request) {
        User user = userRepository.findById(userId).get();
        Inquiry inquiry = InquiryMapper.toInquiry(request, user);
        inquiryRepository.save(inquiry);
        // 프리사이드 url 링크 반환
        return preSignedProvider.createInquiryUploadUrls(request.imageCount(), inquiry.getId());
    }

    // 문의글 전부 조회
    @Transactional(readOnly = true)
    public InquiryAllGetResponse getAllInquiry(Long cursor, int size) {
        Slice<Inquiry> comments = inquiryRepository.findByCursor(cursor, PageRequest.of(0, size));
        Long nextCursor = null;
        if (comments.hasNext()){
            nextCursor = comments.getContent().getLast().getId();
        }

        List<InquiryItem> inquiryItems =  comments.stream().map(inquiry ->
            {
                int answerCount = commentRepository.findAllByInquiryId(inquiry.getId()).size();
                return InquiryMapper.fromInquiryItem(inquiry, answerCount);
            }).toList();

        return new InquiryAllGetResponse(inquiryItems, nextCursor);

    }


    // 본인이 작성한 하나의 문의글 조회 (관리자도 조회 가능)
    @Transactional(readOnly = true)
    public InquiryOneGetResponse getOneInquiry(long userId, Long inquiryId) {
        User user = userRepository.findById(userId).get();
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FIND_INQUIRY));


        //관리자도 아니고 자신이 쓴 문의글이 아니면 조회 불가
        if (!user.getRole().equals(Role.ADMIN) && !inquiry.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        List<String> imagesUrl = imageRepository.findByInquiryImage(inquiry.getId());
        List<String> imagesPathUrl = imagesUrl.stream().map(path-> BASE_URL  + path ).toList();
        return InquiryMapper.fromInquiry(inquiry, imagesPathUrl);


    }


}
