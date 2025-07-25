package shop.matjalalzz.global.security.oauth2.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class GoogleResponseDto implements OAuth2ResponseDto {

    private final Map<String, Object> attribute;

    public GoogleResponseDto(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return "임시 닉네임";
    }
}