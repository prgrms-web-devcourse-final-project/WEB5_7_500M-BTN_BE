package shop.matjalalzz.inquiry.dto;


import java.util.List;
import lombok.Builder;

@Builder
public record InquiryAllGetResponse (
    List<InquiryItem> content,
    Long nextCursor
){}
