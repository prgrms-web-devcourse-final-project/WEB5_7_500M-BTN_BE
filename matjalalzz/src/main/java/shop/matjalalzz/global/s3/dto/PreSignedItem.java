package shop.matjalalzz.global.s3.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record PreSignedItem(

    String key,
    String url

){}
