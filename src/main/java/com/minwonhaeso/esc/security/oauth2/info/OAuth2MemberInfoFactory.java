package com.minwonhaeso.esc.security.oauth2.info;

import com.minwonhaeso.esc.security.oauth2.info.provider.GoogleMemberInfo;
import com.minwonhaeso.esc.security.oauth2.info.provider.KakaoMemberInfo;
import com.minwonhaeso.esc.security.oauth2.info.provider.NaverMemberInfo;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;

import java.util.Map;

public class OAuth2MemberInfoFactory {
    public static OAuth2MemberInfo getOAuth2MemberInfo(ProviderType providerType, Map<String, Object> attributes) {
        switch (providerType) {
            case GOOGLE: return new GoogleMemberInfo(attributes);
            case NAVER: return new NaverMemberInfo(attributes);
            case KAKAO: return new KakaoMemberInfo(attributes);
            default: throw new IllegalArgumentException("Invalid Provider Type");
        }
    }
}