package com.minwonhaeso.esc.member.service;


import com.minwonhaeso.esc.mail.MailService;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.member.repository.redis.MemberEmailRepository;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import com.minwonhaeso.esc.util.JwtTokenUtil;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

}