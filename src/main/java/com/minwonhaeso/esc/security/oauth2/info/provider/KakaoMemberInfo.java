package com.minwonhaeso.esc.security.oauth2.info.provider;


import com.minwonhaeso.esc.security.oauth2.info.OAuth2MemberInfo;

import java.util.Map;

public class KakaoMemberInfo implements OAuth2MemberInfo {
    private final Map<String, Object> attributes;
    private final Map<String, Object> attributesAccount;
    private final Map<String, Object> attributesProfile;

    public KakaoMemberInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.attributesAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.attributesProfile = (Map<String, Object>) attributesAccount.get("profile");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attributesAccount.get("email").toString();
    }

    @Override
    public String getName() {
        return attributesProfile.get("nickname").toString();
    }

    @Override
    public String getImageUrl() {
        return attributesProfile.get("thumbnail_image_url").toString();
    }

    @Override
    public String getProvider() {
        return "Kakao";
    }
}
