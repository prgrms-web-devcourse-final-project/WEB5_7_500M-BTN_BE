package shop.matjalalzz.global.security.oauth2.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final String id;
    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> attribute) {
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