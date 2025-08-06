package shop.matjalalzz.shop.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ShopsResponse (
    Double nextCursor,
    List<ShopsItem> content)
{}

