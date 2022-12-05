package com.minwonhaeso.esc.member.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.member.model.dto.*;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceWithPrincipalTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    //토큰이 필요한 테스트의 경우, 사전 세팅 메소드
    private UserDetails setUserToContextByUsername(String username) {
        CustomerMemberDetailsService customUserDetailsService = new CustomerMemberDetailsService(memberRepository);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities()));
        return userDetails;
    }

    @Test
    void login() {
        //given
        LoginDto.Request request = LoginDto.Request
                .builder()
                .email("ESC@gmail.com")
                .password("1111")
                .build();
        //when
        LoginDto.Response response = memberService.login(request);
        //then
        Optional<RefreshToken> optional = refreshTokenRedisRepository.findById(request.getEmail());
        assertEquals(response.getRefreshToken(), optional.get().getRefreshToken());
    }

    @Test
    void reissue() {
        //given
        String email = "ESC@gmail.com";
        //when
        login();
        setUserToContextByUsername(email);
        RefreshToken before = refreshTokenRedisRepository.findById(email).orElseThrow(() -> new AuthException(AuthErrorCode.EmailNotMatched));
        String refreshToken = "Bearer " + before.getRefreshToken();
        TokenDto after = memberService.reissue(refreshToken);
        //then
        assertEquals(before.getRefreshToken(), after.getRefreshToken());
    }

    @Test
    void info() {
        //given
        String email = "ESC@gmail.com";
        //when
        UserDetails userDetails = setUserToContextByUsername(email);
        InfoDto.Response response = memberService.info(userDetails);
        //then
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotLogIn));
        assertEquals(response.getName(), member.getName());
    }

    @Test
    void patchInfo() {
        //given
        String email = "ESC@gmail.com";
        PatchInfo.Request request = PatchInfo.Request.builder()
                .nickname("ESC 테스트 유저")
                .build();

        //when
        UserDetails userDetails = setUserToContextByUsername(email);
        memberService.patchInfo(userDetails,request);
        //then
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotLogIn));
        assertEquals(member.getNickname(), request.getNickname());
    }
    @Test
    void passwordChange(){
        //given
        String email = "ESC@gmail.com";
        setUserToContextByUsername(email);
        CPasswordDto.Request request = CPasswordDto.Request.builder()
                .email(email)
                .prePassword("1111")
                .newPassword("1234")
                .confirmPassword("1234")
                .build();
        //when
        String uuid =  memberService.changePasswordMail(email);
        memberService.changePasswordMailAuth(uuid);
        memberService.changePassword(request);
        //then
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.EmailNotMatched));
        assertTrue(passwordEncoder.matches("1234",member.getPassword()));
    }
    @Test
    void deleteMember(){
        //given
        String email = "ESC@gmail.com";
        UserDetails userDetails= setUserToContextByUsername(email);
        //when
        memberService.deleteMember(userDetails);
        //then
        Optional<Member> optional = memberRepository.findByEmail(email);
        assertTrue(optional.isEmpty());
    }
}