package shop.matjalalzz.global.security.oauth2.dto;

import java.util.Map;

public class KakaoResponseDto implements OAuth2ResponseDto {

    private final String id;
    private final Map<String, Object> attribute;

    public KakaoResponseDto(Map<String, Object> attribute) {
        this.id = attribute.get("id").toString();
        this.attribute = (Map<String, Object>) attribute.get("kakao_account");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return id;
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