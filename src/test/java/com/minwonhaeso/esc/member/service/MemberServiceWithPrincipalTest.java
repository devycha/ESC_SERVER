package com.minwonhaeso.esc.member.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.member.model.dto.*;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import org.junit.jupiter.api.DisplayName;
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
    @Autowired
    private LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;

    //토큰이 필요한 테스트의 경우,   사전 세팅 메소드
    private UserDetails setUserToContextByUsername(String username) {
        CustomerMemberDetailsService customUserDetailsService = new CustomerMemberDetailsService(memberRepository);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities()));
        return userDetails;
    }

    @Test
    @DisplayName("로그인 + 토큰 재발행 + 로그아웃까지 Test")
    void loginAndOutStory() {
        //given
        String email = "ESC@gmail.com";
        LoginDto.Request request = LoginDto.Request
                .builder()
                .email("ESC@gmail.com")
                .password("1111")
                .build();
        //when
        //1.Login
        LoginDto.Response response = memberService.login(request);
        setUserToContextByUsername(email);
        //2.Reissue
        RefreshToken before = refreshTokenRedisRepository.findById(email).orElseThrow(() -> new AuthException(AuthErrorCode.EmailNotMatched));
        String refreshToken = "Bearer " + before.getRefreshToken();
        TokenDto after = memberService.reissue(refreshToken);
        //3.Logout
        after.setAccessToken("Bearer " + after.getAccessToken());
        memberService.logout(after, email);
        //then
        assertTrue(logoutAccessTokenRedisRepository.existsById(memberService.resolveToken(after.getAccessToken())));
    }

    @Test
    @DisplayName("회원정보 가져오기")
    void info() {
        //given
        String email = "ESC@gmail.com";
        //when
        UserDetails userDetails = setUserToContextByUsername(email);
        InfoDto.Response response = memberService.info(userDetails);
        //then
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotLogIn));
        assertEquals(response.getNickName(), member.getNickname());
    }

    @Test
    @DisplayName("회원정보 수정하기")
    void patchInfo() {
        //given
        String email = "ESC@gmail.com";
        PatchInfo.Request request = PatchInfo.Request.builder()
                .nickname("ESC 테스트 유저")
                .build();

        //when
        UserDetails userDetails = setUserToContextByUsername(email);
        memberService.patchInfo(userDetails, request);
        //then
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotLogIn));
        assertEquals(member.getNickname(), request.getNickname());
    }

    @Test
    @DisplayName("비밀번호 변경")
    void passwordChange() {
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
        String uuid = memberService.changePasswordMail(email);
        memberService.changePasswordMailAuth(uuid);
        memberService.changePassword(request);
        //then
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.EmailNotMatched));
        assertTrue(passwordEncoder.matches("1234", member.getPassword()));
    }

    @Test
    @DisplayName("회원 탈퇴")
    void deleteMember() {
        //given
        String email = "ESC@gmail.com";
        UserDetails userDetails = setUserToContextByUsername(email);
        //when
        memberService.deleteMember(userDetails);
        //then
        Optional<Member> optional = memberRepository.findByEmail(email);
        assertTrue(optional.isEmpty());
    }
}