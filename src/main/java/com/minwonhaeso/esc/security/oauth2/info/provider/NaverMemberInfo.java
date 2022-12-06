package com.minwonhaeso.esc.security.oauth2.info.provider;

import com.minwonhaeso.esc.security.oauth2.info.OAuth2MemberInfo;

import java.util.Map;

public class NaverMemberInfo implements OAuth2MemberInfo {

    private final Map<String, Object> attributes;

    public NaverMemberInfo(Map<String, Object> attributes) {
        this.attributes = (Map<String, Object>) attributes.get("response");
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
    public String getName() {
        return attributes.get("name").toString();
    }

    @Override
    public String getImageUrl() {
        return attributes.get("profile_image").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getProvider() {
        return "Naver";
    }
}
