package shop.matjalalzz.inquiry.dto;


import java.util.List;

public record InquiryOneGetResponse(

    String title,
    String content,
    List<String> images)
{}
