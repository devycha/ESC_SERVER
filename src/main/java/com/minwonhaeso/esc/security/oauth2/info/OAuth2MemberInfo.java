package com.minwonhaeso.esc.security.oauth2.info;

import java.util.Map;

public interface OAuth2MemberInfo {

    Map<String, Object> getAttributes();
    String getProviderId();
    String getProvider();
    String getEmail();
    String getName();

    String getImageUrl();
}