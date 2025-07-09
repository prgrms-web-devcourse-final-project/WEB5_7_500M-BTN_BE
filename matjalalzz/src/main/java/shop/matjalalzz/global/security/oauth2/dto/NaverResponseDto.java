package shop.matjalalzz.global.security.oauth2.dto;

import java.util.Map;

public class NaverResponseDto implements OAuth2ResponseDto {

    private final Map<String, Object> attribute;

    public NaverResponseDto(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }
}