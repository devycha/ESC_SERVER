package com.minwonhaeso.esc.member.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.member.model.dto.LoginDto;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.dto.TokenDto;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.member.repository.MemberEmailRepository;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberEmailRepository memberEmailRepository;
    @Autowired
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Test
    void emailDuplicateYn (){
        //given
        String email = "ESC@gmail.com";
        //when
        memberService.emailDuplicateYn(email);
        //then
        Optional<Member> member =  memberRepository.findByEmail(email);
        assertTrue(member.isEmpty());
    }
    @Test
    void deliverEmailAuthCode(){
        //given
        String email = "ESC@gmail.com";
        //when
        String key = memberService.deliverEmailAuthCode(email);

        //then
        Optional<MemberEmail> optional =  memberEmailRepository.findById(key);
        assertTrue(optional.isPresent());
    }

    @Test
    void emailAuthentication(){
        //given
        // 위 deliverEmailAuthCode 에서 받은 값을 사용.
        String key = "614789";
        //when
        memberService.emailAuthentication(key);
        Optional<MemberEmail> optional = memberEmailRepository.findById(key);
        //then
        assertTrue(optional.isPresent());
    }

    @Test
    void signUser (){
        //given
        SignDto.Request signDto = SignDto.Request.builder()
                .email("ESC@gmail.com")
                .name("ESC_TEST")
                .password("1111")
                .type(MemberType.ADMIN)
                .nickname("ESC")
                .image("/ESC/test/image.img")
                .key("614789")
                .build();
        //when
        SignDto.Response response = memberService.signUser(signDto);
        //then
        Member member = memberRepository.findByEmail(signDto.getEmail()).orElseThrow(()-> new AuthException(AuthErrorCode.EmailNotMatched));
        assertEquals(member.getName(), response.getName());
    }
}