package shop.matjalalzz.review.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ReviewProjection {

    private Long reviewId;
    private Long writerId;
    private Double rating;
    private String content;
    private LocalDateTime createdAt;
    private String userNickname;
    private List<String> images;

}

