package com.minwonhaeso.esc.member.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

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