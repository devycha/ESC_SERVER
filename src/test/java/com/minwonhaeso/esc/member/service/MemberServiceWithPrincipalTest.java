package com.minwonhaeso.esc.member.service;


import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import com.minwonhaeso.esc.mail.MailService;
import com.minwonhaeso.esc.member.model.dto.CPasswordDto;
import com.minwonhaeso.esc.member.model.dto.InfoDto;
import com.minwonhaeso.esc.member.model.dto.PatchInfo;
import com.minwonhaeso.esc.member.model.dto.TokenDto;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.member.repository.redis.MemberEmailRepository;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservation;
import com.minwonhaeso.esc.stadium.model.type.PaymentType;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceWithPrincipalTest {
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

    @DisplayName("로그아웃 성공")
    @Test
    void logout(){
        String userName = "test@gmail.com";
        TokenDto tokenDto = TokenDto.builder()
                .accessToken("asdf.asdf.asdf")
                .refreshToken("fdsa.fdsa.fdsa")
                .grantType("ROLE_USER")
                .build();
        //given

        //when
        Map<String, String> result =  memberService.logout(tokenDto, userName);
        //then
        assertNotNull(result.get("message"));
    }

    @Test
    void reissue_Success(){
        String refreshToken = "Bearer asdf.asdf.asdf";
        String newAccessToken = "Bearer qwer.qwer.qwer";
        RefreshToken token = RefreshToken.builder()
                .id("test@gmail.com")
                .refreshToken("asdf.asdf.asdf")
                .expiration(100L)
                .build();
        //given
        given(jwtTokenUtil.getUsername(anyString())).willReturn("test@gmail.com");
        given(refreshTokenRedisRepository.findById(anyString()))
                .willReturn(Optional.of(token));
        given(jwtTokenUtil.generateAccessToken(anyString()))
                .willReturn(newAccessToken);
        given(jwtTokenUtil.saveRefreshToken(anyString()))
                .willReturn(token);

        //when
        TokenDto result =  memberService.reissue(refreshToken);
        //then
        assertNotNull(result.getAccessToken());
    }

    @Test
    void reissue_Fail_AccessTokenAlreadyExpired(){
        String refreshToken = "Bearer asdf.asdf.asdf";
        //given
        given(jwtTokenUtil.getUsername(anyString())).willReturn("test@gmail.com");
        given(refreshTokenRedisRepository.findById(anyString()))
                .willReturn(Optional.empty());
        //when
        AuthException exception = assertThrows(AuthException.class,
                ()->  memberService.reissue(refreshToken));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.AccessTokenAlreadyExpired);
    }

    @Test
    void reissue_Fail_RefreshTokenNotMatch(){
        String refreshToken = "Bearer asdf.asdf.asdf";
        String newAccessToken = "Bearer qwer.qwer.qwer";
        RefreshToken token = RefreshToken.builder()
                .id("test@gmail.com")
                .refreshToken("asdf.asdf.asdf2")
                .expiration(100L)
                .build();
        //given
        given(jwtTokenUtil.getUsername(anyString())).willReturn("test@gmail.com");
        given(refreshTokenRedisRepository.findById(anyString()))
                .willReturn(Optional.of(token));

        //when
        AuthException exception = assertThrows(AuthException.class,
                ()-> memberService.reissue(refreshToken));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.TokenNotMatch);
    }

    @Test
    void getInfo_Success(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .build();
        //given

        //when
        InfoDto.Response result =  memberService.info(member);
        //then
        assertEquals(member.getMemberId(), result.getId());
    }

    @Test
    void getPatch(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .build();
        PatchInfo.Request request = PatchInfo.Request.builder()
                .nickname("뽀로로")
                .imgUrl("house")
                .build();
        //given

        //when
        PatchInfo.Request result =  memberService.patchInfo(member,request);
        //then
        assertEquals(result.getNickname(), request.getNickname());
    }

    @Test
    void deleteMember_Success_Type_User(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.USER)
                .build();
        List<StadiumReservation> reservations = new ArrayList<>();
        //given
        given(stadiumReservationRepository.findALlByMember(member))
                .willReturn(reservations);
        //when
        Map<String, String> result =  memberService.deleteMember(member);
        //then
        assertNotNull(result.get("message"));
    }

    @Test
    void deleteMember_Success_Type_Manager(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.MANAGER)
                .build();
        List<Stadium> stadiums = new ArrayList<>();
        //given
        given(stadiumRepository.findAllByMember(member))
                .willReturn(stadiums);

        //when
        Map<String, String> result =  memberService.deleteMember(member);
        //then
        assertNotNull(result.get("message"));
    }

    @Test
    void deleteMember_Fail_HasReservation_Manager(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.MANAGER)
                .build();
        List<StadiumReservation> reservations = new ArrayList<>();
        reservations.add(new StadiumReservation());
        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.RT19)
                .closeTime(ReservingTime.RT37)
                .reservations(reservations)
                .build();
        List<Stadium> stadiums = new ArrayList<>();
        stadiums.add(stadium);
        //given
        given(stadiumRepository.findAllByMember(member))
                .willReturn(stadiums);
        //when
        StadiumException exception = assertThrows(StadiumException.class, ()-> memberService.deleteMember(member));
        //then
        assertEquals(exception.getErrorCode(), StadiumErrorCode.HasReservation);
    }

    @Test
    void deleteMember_Fail_User(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.USER)
                .build();
        StadiumReservation reservation = StadiumReservation.builder()
                .id(1L)
                .paymentType(PaymentType.CARD)
                .status(StadiumReservationStatus.RESERVED)
                .headCount(2)
                .price(10000)
                .build();

        List<StadiumReservation> reservations = new ArrayList<>();
        reservations.add(reservation);
        //given
        given(stadiumReservationRepository.findALlByMember(member))
                .willReturn(reservations);
        //when
        StadiumException exception = assertThrows(StadiumException.class, ()-> memberService.deleteMember(member));
        //then
        assertEquals(exception.getErrorCode(), StadiumErrorCode.HasReservation);
    }

    @Test
    void changePasswordMailAuth_Success(){
        String key = "12312434132123";
        MemberEmail memberEmail = MemberEmail.builder()
                .id(key)
                .email("phc09188@gmail.com")
                .expireDt(100L)
                .build();

        //given
        given(memberEmailRepository.findById(anyString()))
                .willReturn(Optional.of(memberEmail));
        //when
        Map<String,String> result = memberService.changePasswordMailAuth(key);
        //then
        assertNotNull(result.get("message"));
    }

    @Test
    void changePasswordMailAuth_Fail_EmailAuthTimeOut(){
        String key = "12312434132123";


        //given
        given(memberEmailRepository.findById(anyString()))
                .willReturn(Optional.empty());
        //when
        AuthException exception = assertThrows(AuthException.class,
                () -> memberService.changePasswordMailAuth(key));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.EmailAuthTimeOut);
    }
    @Test
    void changePasswordMailAuth_Fail_AuthKeyNotMatch(){
        String key = "12312434132123";
        MemberEmail memberEmail = MemberEmail.builder()
                .id(key +"1111")
                .email("phc09188@gmail.com")
                .expireDt(100L)
                .build();
        //given
        given(memberEmailRepository.findById(anyString()))
                .willReturn(Optional.of(memberEmail));
        //when
        AuthException exception = assertThrows(AuthException.class,
                () -> memberService.changePasswordMailAuth(key));
        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.AuthKeyNotMatch);
    }

    @Test
    void changePassword_HasNotToken_Success(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.USER)
                .build();
        CPasswordDto.Request request = CPasswordDto.Request.builder()
                .email("ESC@gmail.com")
                .prePassword("1111")
                .newPassword("1234")
                .confirmPassword("1234")
                .hasToken(false)
                .build();
        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));
        given(passwordEncoder.encode(anyString())).willReturn("asdf");
        //when
        Map<String, String> result = memberService.changePassword(request);
        //then
        assertNotNull(result.get("message"));
    }

    @Test
    void changePassword_HasToken_Success(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.USER)
                .build();
        CPasswordDto.Request request = CPasswordDto.Request.builder()
                .email("ESC@gmail.com")
                .prePassword("1111")
                .newPassword("1234")
                .confirmPassword("1234")
                .hasToken(true)
                .build();
        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);
        given(passwordEncoder.encode(anyString())).willReturn("asdf");
        //when
        Map<String, String> result = memberService.changePassword(request);
        //then
        assertNotNull(result.get("message"));
    }
    @Test
    void changePassword_HasNotToken_Fail_PasswordNotEqual(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.USER)
                .build();
        CPasswordDto.Request request = CPasswordDto.Request.builder()
                .email("ESC@gmail.com")
                .prePassword("1111")
                .newPassword("1234")
                .confirmPassword("12345")
                .hasToken(false)
                .build();
        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));
        //when
        AuthException exception = assertThrows(AuthException.class,() -> memberService.changePassword(request));

        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.PasswordNotEqual);
    }
    @Test
    void changePassword_HasToken_Fail_PasswordNotEqual1(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.USER)
                .build();
        CPasswordDto.Request request = CPasswordDto.Request.builder()
                .email("ESC@gmail.com")
                .prePassword("1111")
                .newPassword("1234")
                .confirmPassword("12345")
                .hasToken(true)
                .build();
        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);
        //when
        AuthException exception = assertThrows(AuthException.class,() -> memberService.changePassword(request));

        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.PasswordNotEqual);
    }
    @Test
    void changePassword_HasToken_Fail_PasswordNotEqual2(){
        Member member = Member.builder()
                .memberId(3L)
                .name("제로")
                .nickname("제로로")
                .imgUrl("https:/ESC/kadfl")
                .password("1111")
                .email("ESC@gmail.com")
                .type(MemberType.USER)
                .build();
        CPasswordDto.Request request = CPasswordDto.Request.builder()
                .email("ESC@gmail.com")
                .prePassword("1111")
                .newPassword("1234")
                .confirmPassword("12345")
                .hasToken(true)
                .build();
        //given
        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);
        //when
        AuthException exception = assertThrows(AuthException.class,() -> memberService.changePassword(request));

        //then
        assertEquals(exception.getErrorCode(), AuthErrorCode.PasswordNotEqual);
    }
}