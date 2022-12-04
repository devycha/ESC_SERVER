package com.minwonhaeso.esc.security.oauth2.handler;

import com.minwonhaeso.esc.security.auth.jwt.JwtTokenUtil;
import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.oauth2.info.OAuth2MemberInfo;
import com.minwonhaeso.esc.security.oauth2.info.OAuth2MemberInfoFactory;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import com.minwonhaeso.esc.security.oauth2.util.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.minwonhaeso.esc.security.auth.jwt.JwtExpirationEnums.REFRESH_TOKEN_EXPIRATION_TIME;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${test.url}")
    private String basicUrl;

    @Value("${spring.jwt.refresh-token.cookie}")
    private String refreshTokenForCookie;

    private final JwtTokenUtil jwtTokenUtil;

    public OAuth2SuccessHandler(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        ProviderType providerType = ProviderType.valueOf(authToken.getAuthorizedClientRegistrationId().toUpperCase());
        OAuth2MemberInfo memberInfo = OAuth2MemberInfoFactory.getOAuth2MemberInfo(providerType, oAuth2User.getAttributes());

        log.info("Generate Token");

        String memberEmail = memberInfo.getEmail();
        String token = jwtTokenUtil.generateAccessToken(memberEmail);
        RefreshToken refreshToken = jwtTokenUtil.saveRefreshToken(memberEmail);

        int cookieMaxAge = (int) (REFRESH_TOKEN_EXPIRATION_TIME.getValue() / 60);
        CookieUtil.addCookie(response, refreshTokenForCookie, refreshToken.getRefreshToken(), cookieMaxAge);

        String targetUrl = makeRedirectUrl(token);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String makeRedirectUrl(String token) {
        return UriComponentsBuilder.fromUriString(basicUrl)
                .queryParam("token", token)
                .build().toUriString();
    }

}
