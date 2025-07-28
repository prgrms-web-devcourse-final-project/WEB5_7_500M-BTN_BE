package shop.matjalalzz.inquiry.mapper;


import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import shop.matjalalzz.inquiry.dto.InquiryAllGetResponse;
import shop.matjalalzz.inquiry.dto.InquiryCreateRequest;
import shop.matjalalzz.inquiry.dto.InquiryItem;
import shop.matjalalzz.inquiry.dto.InquiryOneGetResponse;
import shop.matjalalzz.inquiry.entity.Inquiry;
import shop.matjalalzz.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryMapper {
    public static Inquiry toInquiry(InquiryCreateRequest inquiryCreateRequest, User user) {
        return Inquiry.builder()
            .title(inquiryCreateRequest.title())
            .content(inquiryCreateRequest.content())
            .user(user)
            .build();
    }

    public static InquiryOneGetResponse fromInquiry(Inquiry inquiry, List<String> images) {
        return new InquiryOneGetResponse(inquiry.getTitle(), inquiry.getContent(), images);
    }

    public static InquiryItem fromInquiryItem(Inquiry inquiry, int answerCount) {
        return InquiryItem.builder()
            .InquiryId(inquiry.getId())
            .title(inquiry.getTitle())
            .createTime(inquiry.getCreatedAt())
            .answerCount(answerCount)
            .build();
    }

}
