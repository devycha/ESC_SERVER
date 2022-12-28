package com.minwonhaeso.esc.member.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.mail.MailService;
import com.minwonhaeso.esc.member.model.dto.LoginDto;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.member.repository.redis.MemberEmailRepository;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import com.minwonhaeso.esc.util.JwtTokenUtil;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

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
    @Mock
    public StadiumRepository stadiumRepository;
    @Mock
    public StadiumReservationRepository stadiumReservationRepository;

    @Before("")
    public void setup() {
        MockitoAnnotations.initMocks(this);
        memberService = new MemberService(memberRepository, passwordEncoder, mailService, memberEmailRepository,
                refreshTokenRedisRepository, logoutAccessTokenRedisRepository, jwtTokenUtil, stadiumRepository, stadiumReservationRepository);
    }


    @DisplayName("이메일 중복확인 성공")
    @Test
    void emailDuplicate_Success() {
        String email = "ESC@gmail.com";
        //given
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());
        //when
        Map<String, String> result = memberService.emailDuplicateYn(email);
        //then
        assertNotNull(result.get("message"));
    }

    @DisplayName("이메일 중복확인 실패 - 존재하는 이메일")
    @Test
    void emailDuplicate_Fail() {
        String email = "ESC@gmail.com";
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .password("1111")
                .email("ESC@gmail.com")
                .build();
        //given
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
        //when
        AuthException exception = assertThrows(AuthException.class,
                () -> memberService.emailDuplicateYn(email));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.EmailAlreadySignUp);
    }

    @DisplayName("이메일 코드 인증 - 성공")
    @Test
    void emailAuthentication_Success() {
        String key = "111111";
        MemberEmail memberEmail = MemberEmail.builder()
                .email("test@gmail.com")
                .expireDt(100L)
                .id(key)
                .build();

        //given
        given(memberEmailRepository.findById(anyString()))
                .willReturn(Optional.of(memberEmail));
        //when
        Map<String, String> result = memberService.emailAuthentication(key);
        //then
        assertNotNull(result.get("message"));
    }

    @DisplayName("이메일 코드 인증 실패 - 코드 번호가 다름")
    @Test
    void emailAuthentication_Fail_CodeNotMatch() {
        String key = "111111";
        //given
        given(memberEmailRepository.findById(anyString()))
                .willReturn(Optional.empty());
        //when
        AuthException exception = assertThrows(AuthException.class, () -> memberService.emailAuthentication(key));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.AuthKeyNotMatch);
    }

    @DisplayName("회원가입 실패 - 인증 키가 맞지 않습니다.")
    @Test
    void signUser_Fail() {
        String key = "123456";

        SignDto.Request request = SignDto.Request.builder()
                .type("USER")
                .email("test@gmail.com")
                .name("ESC")
                .password("1234")
                .nickname("TEST")
                .image("https:/ESC/image")
                .key(key)
                .build();

        //given
        given(memberEmailRepository.findById(anyString()))
                .willReturn(Optional.empty());
        //when
        AuthException exception = assertThrows(AuthException.class, () -> memberService.signUser(request));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.AuthKeyNotMatch);
    }

    @DisplayName("회원가입 실패 - 인증 키 값이 일치하지 않습니다.(다른 사람이 발급한 코드)")
    @Test
    void signUser_Fail_EmailNotMatch() {
        String key = "123456";
        SignDto.Request request = SignDto.Request.builder()
                .type("USER")
                .email("test@gmail.com")
                .name("ESC")
                .password("1234")
                .nickname("TEST")
                .image("https:/ESC/image")
                .key(key)
                .build();
        //given
        //when
        AuthException exception = assertThrows(AuthException.class, () -> memberService.signUser(request));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.AuthKeyNotMatch);
    }

    @DisplayName("회원가입 - 성공")
    @Test
    void signUser_Success() {
        String key = "123456";
        MemberEmail memberEmail = MemberEmail.builder()
                .email("test@gmail.com")
                .expireDt(100L)
                .id("123456")
                .build();
        SignDto.Request request = SignDto.Request.builder()
                .type("USER")
                .email("test@gmail.com")
                .name("ESC")
                .password("1234")
                .nickname("TEST")
                .image("https:/ESC/image")
                .key(key)
                .build();
        String password = "akjldsfajwnl;kfsnf;wa";

        //given
        given(memberEmailRepository.findById(anyString()))
                .willReturn(Optional.of(memberEmail));
        given(passwordEncoder.encode(anyString()))
                .willReturn(password);
        //when
        SignDto.Response response = memberService.signUser(request);
        //then
        assertEquals(response.getNickname(), request.getNickname());
    }

    @DisplayName("로그인 성공")
    @Test
    void login_Success() {
        LoginDto.Request request = LoginDto.Request.builder()
                .email("test@gmail.com")
                .password("1111")
                .type("USER")
                .build();
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .type(MemberType.USER)
                .build();
        String password = "akjldsfajwnl;kfsnf;wa";
        String accessToken = "asdf.asdf.asdf";
        RefreshToken refreshToken = RefreshToken.builder()
                .id("test@gmail.com")
                .refreshToken("fdsa.fdsa.fdsa")
                .expiration(100L).build();
        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);
        given(jwtTokenUtil.generateAccessToken(anyString()))
                .willReturn(accessToken);
        given(jwtTokenUtil.saveRefreshToken(anyString()))
                .willReturn(refreshToken);
        //when
        LoginDto.Response response = memberService.login(request);
        //then
        assertEquals(refreshToken.getRefreshToken(), response.getRefreshToken());
    }

    @DisplayName("로그인 실패 - 일치하는 회원이 없습니다,")
    @Test
    void login_Fail_MemberNotFound() {
        LoginDto.Request request = LoginDto.Request.builder()
                .email("test@gmail.com")
                .password("1111")
                .type("USER")
                .build();

        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        //when
        AuthException exception = assertThrows(AuthException.class, () -> memberService.login(request));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.MemberNotFound);
    }

    @DisplayName("로그인 실패 - 회원가입한 타입과 다르게 로그인함")
    @Test
    void login_Fail_MemberTypeNotMatch() {
        LoginDto.Request request = LoginDto.Request.builder()
                .email("test@gmail.com")
                .password("1111")
                .type("MANAGER")
                .build();
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .type(MemberType.USER)
                .build();

        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(),anyString()))
                .willReturn(true);
        //when
        AuthException exception = assertThrows(AuthException.class, ()-> memberService.login(request));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.MemberTypeNotMatch);
    }

    @DisplayName("로그인 실패 - 패스워드를 다시 입력해주세요.")
    @Test
    void login_Fail_PasswordNotEqual() {
        LoginDto.Request request = LoginDto.Request.builder()
                .email("test@gmail.com")
                .password("1111")
                .type("MANAGER")
                .build();
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .type(MemberType.USER)
                .build();

        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));
        //when
        AuthException exception = assertThrows(AuthException.class, ()-> memberService.login(request));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.PasswordNotEqual);
    }



}