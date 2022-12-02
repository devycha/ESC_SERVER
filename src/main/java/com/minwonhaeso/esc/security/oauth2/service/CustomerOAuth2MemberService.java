package com.minwonhaeso.esc.security.oauth2.service;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.type.MemberRole;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.auth.PrincipalDetails;
import com.minwonhaeso.esc.security.auth.jwt.JwtTokenUtil;
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
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
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
        OAuth2MemberInfo oAuth2MemberInfo = OAuth2MemberInfoFactory.getOAuth2MemberInfo(providerType, oAuth2User.getAttributes());

        Optional<Member> optionalMember = memberRepository.findByEmail(oAuth2MemberInfo.getEmail());
        Member member;

        if (optionalMember.isPresent()) {
            member = optionalMember.get();

//            TODO - 기존에 OAUTH로 가입한 providerType이 다를 경우 예외 처리
//            if (!member.getProviderType().equals(providerType)) {
//                throw new RuntimeException();
//            }

//            member = updateMember(member, oAuth2UserInfo);

        } else {
            member = registerMember(oAuth2MemberInfo, providerType);
            return new PrincipalDetails(member, oAuth2User.getAttributes());
        }

        log.info("processOAuth2User :" + member);

        // TODO
        // String accessToken = jwtTokenUtil.generateAccessToken(oAuth2MemberInfo.getEmail());
        // RefreshToken refreshToken = jwtTokenUtil.saveRefreshToken(oAuth2MemberInfo.getEmail());

        return new PrincipalDetails(member, oAuth2User.getAttributes());
    }

    private Member registerMember(OAuth2MemberInfo oAuth2MemberInfo, ProviderType providerType) {

        String uuid = UUID.randomUUID().toString().substring(0, 6);

        return memberRepository.saveAndFlush(Member.builder()
                .email(oAuth2MemberInfo.getEmail())
                .name(oAuth2MemberInfo.getName())
                .nickname(providerType+"_"+ oAuth2MemberInfo.getProviderId())
                .password(BCrypt.hashpw("esc" + uuid, BCrypt.gensalt()))
                .role(MemberRole.ROLE_USER)
                .providerType(providerType)
                .status(MemberStatus.ING)
                .type(MemberType.USER)
                .providerId(oAuth2MemberInfo.getProviderId())
                .imgUrl(oAuth2MemberInfo.getImageUrl())
                .build());
    }

//    TODO - OAUTH2_정보 수정
//    private Member updateMember(Member member, OAuth2UserInfo oAuth2UserInfo) {
//        member.updateName(oAuth2UserInfo.getName());
//        member.updateImageUrl(oAuth2UserInfo.getImageUrl());
//
//        return memberRepository.save(member);
//    }
}
