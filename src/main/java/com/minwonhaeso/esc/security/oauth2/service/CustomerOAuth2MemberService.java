package com.minwonhaeso.esc.security.oauth2.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.type.MemberRole;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.security.oauth2.info.OAuth2MemberInfo;
import com.minwonhaeso.esc.security.oauth2.info.OAuth2MemberInfoFactory;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomerOAuth2MemberService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            log.info(String.valueOf(userRequest.getAccessToken()));
            return this.processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

        ProviderType providerType = ProviderType.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        OAuth2MemberInfo memberInfo = OAuth2MemberInfoFactory.getOAuth2MemberInfo(providerType, oAuth2User.getAttributes());

        Optional<Member> savedMember = memberRepository.findByEmail(memberInfo.getEmail());
        Member member;

        if (savedMember.isPresent()) {
            member = savedMember.get();

            if (!member.getProviderType().equals(providerType)) {
                throw new AuthException(AuthErrorCode.OAuthProviderMissMatch);
            }
            updateMember(member, memberInfo);

        } else {
            member = registerMember(memberInfo, providerType);
        }

        log.info("["+providerType.toString() + "] processOAuth2User :" + member);

        return new PrincipalDetail(member, oAuth2User.getAttributes());
    }

    private Member registerMember(OAuth2MemberInfo memberInfo, ProviderType providerType) {

        String uuid = UUID.randomUUID().toString().substring(0, 6);

        return memberRepository.saveAndFlush(Member.builder()
                .email(memberInfo.getEmail())
                .name(memberInfo.getName())
                .nickname(providerType+"_"+ memberInfo.getProviderId())
                .password(BCrypt.hashpw("esc" + uuid, BCrypt.gensalt()))
                .role(MemberRole.ROLE_USER)
                .providerType(providerType)
                .status(MemberStatus.ING)
                .type(MemberType.USER)
                .providerId(memberInfo.getProviderId())
                .imgUrl(memberInfo.getImageUrl())
                .build());
    }

    private void updateMember(Member member, OAuth2MemberInfo memberInfo) {
        if (memberInfo.getName() != null && !memberInfo.getName().equals(memberInfo.getName())) {
            member.setName(memberInfo.getName());
        }

        if (memberInfo.getImageUrl() != null && !memberInfo.getImageUrl().equals(memberInfo.getImageUrl())) {
            member.setImgUrl(memberInfo.getImageUrl());
        }

        memberRepository.save(member);
    }
}
