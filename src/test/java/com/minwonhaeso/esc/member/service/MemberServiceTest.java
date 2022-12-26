package com.minwonhaeso.esc.member.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.mail.MailService;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.member.repository.redis.MemberEmailRepository;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import com.minwonhaeso.esc.util.JwtTokenUtil;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @InjectMocks
    public MemberService memberService;

    @Mock
    public MemberRepository memberRepository;

    @Mock
    public MailService mailService;
    @Mock
    public PasswordEncoder passwordEncoder;
    @Mock
    public MemberEmailRepository memberEmailRepository;
    @Mock
    public RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Mock
    public LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
    @Mock
    public JwtTokenUtil jwtTokenUtil;

    @Before("")
    public void setup(){
        MockitoAnnotations.initMocks(this);
        memberService = new MemberService(memberRepository,passwordEncoder,mailService,memberEmailRepository,refreshTokenRedisRepository,logoutAccessTokenRedisRepository,jwtTokenUtil);
    }

    @Test
    @DisplayName("이메일 인증부터 회원가입까지의 스토리 Test")
    void memberSignUpStory() {
        //given
        String email = "ESC@gmail.com";
        SignDto.Request signDto = SignDto.Request.builder()
                .email("ESC@gmail.com")
                .name("ESC_TEST")
                .password("1111")
                .type("USER")
                .nickname("ESC")
                .image("/ESC/test/image.img")
                .build();
        //when
        memberService.emailDuplicateYn(email);
        String key = memberService.deliverEmailAuthCode(email);
        memberService.emailAuthentication(key);
        signDto.setKey(key);
        SignDto.Response response = memberService.signUser(signDto);
        //then
        Member member = memberRepository.findByEmail(signDto.getEmail()).orElseThrow(() -> new AuthException(AuthErrorCode.EmailNotMatched));
        assertEquals(member.getName(), response.getName());
    }
}