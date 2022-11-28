package com.minwonhaeso.esc.member.service;


import com.minwonhaeso.esc.component.MailComponents;
import com.minwonhaeso.esc.member.model.dto.LoginDto;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.dto.TokenDto;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import com.minwonhaeso.esc.member.repository.MemberEmailRepository;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.jwt.JwtExpirationEnums;
import com.minwonhaeso.esc.security.jwt.JwtTokenUtil;
import com.minwonhaeso.esc.security.redis.LogoutAccessToken;
import com.minwonhaeso.esc.security.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.security.redis.RefreshToken;
import com.minwonhaeso.esc.security.redis.RefreshTokenRedisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.minwonhaeso.esc.security.jwt.JwtExpirationEnums.REFRESH_TOKEN_EXPIRATION_TIME;

@Service
public class MemberService {
    @Value("${spring.mail.domain}")
    private String domain;
    private final Long emailExpiredTime = 1000L * 60 * 60 * 2;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, MailComponents mailComponents,
                         MemberEmailRepository memberEmailRepository, RefreshTokenRedisRepository refreshTokenRedisRepository,
                         LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository, JwtTokenUtil jwtTokenUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailComponents = mailComponents;
        this.memberEmailRepository = memberEmailRepository;
        this.refreshTokenRedisRepository = refreshTokenRedisRepository;
        this.logoutAccessTokenRedisRepository = logoutAccessTokenRedisRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailComponents mailComponents;
    private final MemberEmailRepository memberEmailRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public void signUser(SignDto signDto) {
        Optional<MemberEmail> memberEmail = memberEmailRepository.findById(signDto.getKey());
        if(memberEmail.isEmpty()){
            throw new MailAuthenticationException("메일인증을 진행해주세요.");
        }
        memberEmailRepository.delete(memberEmail.get());
        signDto.setPassword(passwordEncoder.encode(signDto.getPassword()));
        Member member = Member.of(signDto);
        memberRepository.save(member);
    }

    public void emailDuplicateYn(String email) {
        Optional<Member> optional = memberRepository.findByEmail(email);
        if (optional.isPresent()) {
            throw new RuntimeException("사용할 수 없는 이메일입니다.");
        }
    }

    public void deliverEmailAuthCode(String email) {
        String uuid = UUID.randomUUID().toString();
        MemberEmail memberEmail = MemberEmail.createEmailAuthKey(email,uuid,emailExpiredTime);
        String subject = "[ESC] 이메일 인증 안내";
        String content = "<p>아래 링크를 통해 인증을 완료해주세요. </p><a href='"+ domain
                + "'" + uuid + "> 인증 </a>";
        mailComponents.sendMail(email,subject,content);
        memberEmailRepository.save(memberEmail);
    }

    public String emailAuthentication(String key) {
        MemberEmail memberEmail = memberEmailRepository.findById(key).orElseThrow(() -> new MailAuthenticationException("인증 시간이 만료되었습니다. 다시 인증해주세요."));
        memberEmail.setAuthYn(true);
        memberEmailRepository.save(memberEmail);
        return memberEmail.getId();
    }

    public LoginDto.Response login(LoginDto.Request loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail()).
                orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 아이디가 없습니다."));
        checkPassword(loginDto.getPassword(), member.getPassword());
        String username = member.getUsername();
        String accessToken = jwtTokenUtil.generateAccessToken(username);
        RefreshToken refreshToken = saveRefreshToken(username);
        return LoginDto.Response.of(username, member.getImgUrl(), accessToken, refreshToken.getRefreshToken());
    }

    private RefreshToken saveRefreshToken(String username) {
        return refreshTokenRedisRepository.save(RefreshToken.createRefreshToken(username,
                jwtTokenUtil.generateRefreshToken(username), REFRESH_TOKEN_EXPIRATION_TIME.getValue()));
    }

    private void checkPassword(String rawPassword, String findMemberPassword) {
        if (!passwordEncoder.matches(rawPassword, findMemberPassword)) {
            throw new IllegalArgumentException("비밀번호가 맞지 않습니다.");
        }
    }

    public void logout(TokenDto tokenDto, String username) {
        String accessToken = resolveToken(tokenDto.getAccessToken());
        long remainMilliSeconds = jwtTokenUtil.getRemainMilliSeconds(accessToken);
        refreshTokenRedisRepository.deleteById(username);
        logoutAccessTokenRedisRepository.save(LogoutAccessToken.of(accessToken, username, remainMilliSeconds));
    }

    private String resolveToken(String token) {
        return token.substring(7);
    }

    public TokenDto reissue(String refreshToken) {
        refreshToken = resolveToken(refreshToken);
        String username = getCurrentUsername();
        RefreshToken redisRefreshToken = refreshTokenRedisRepository.findById(username).orElseThrow(() -> new NoSuchElementException("인증이 만료되었습니다. 다시 로그인해주세요."));
        if (refreshToken.equals(redisRefreshToken.getRefreshToken())) {
            return reissueRefreshToken(refreshToken, username);
        }
        throw new IllegalArgumentException("토큰이 일치하지 않습니다.");
    }

    private TokenDto reissueRefreshToken(String refreshToken, String username) {
        if (lessThanReissueExpirationTimesLeft(refreshToken)) {
            String accessToken = jwtTokenUtil.generateAccessToken(username);
            return TokenDto.of(accessToken, saveRefreshToken(username).getRefreshToken());
        }
        return TokenDto.of(jwtTokenUtil.generateAccessToken(username), refreshToken);
    }

    private boolean lessThanReissueExpirationTimesLeft(String refreshToken) {
        return jwtTokenUtil.getRemainMilliSeconds(refreshToken) < JwtExpirationEnums.REISSUE_EXPIRATION_TIME.getValue();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return principal.getUsername();
    }
}
