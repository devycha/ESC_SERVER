package com.minwonhaeso.esc.member.service;


import com.minwonhaeso.esc.component.MailComponents;
import com.minwonhaeso.esc.member.model.dto.*;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import javax.security.auth.callback.PasswordCallback;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.minwonhaeso.esc.security.jwt.JwtExpirationEnums.REFRESH_TOKEN_EXPIRATION_TIME;

@Service
public class MemberService {
    @Value("${spring.mail.domain}")
    private String emailAuthDomain;
    @Value("${spring.mail.password.domain}")
    private String changePasswordEmailDomain;

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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SignDto.Response signUser(SignDto.Request signDto) {
        Optional<MemberEmail> memberEmail = memberEmailRepository.findById(signDto.getKey());
        if (memberEmail.isEmpty()) {
            throw new MailAuthenticationException("메일인증을 진행해주세요.");
        }
        memberEmailRepository.delete(memberEmail.get());
        signDto.setPassword(passwordEncoder.encode(signDto.getPassword()));
        Member member = Member.of(signDto);
        memberRepository.save(member);
        return SignDto.Response.builder()
                .name(member.getName())
                .image(member.getImgUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public void emailDuplicateYn(String email) {
        Optional<Member> optional = memberRepository.findByEmail(email);
        if (optional.isPresent()) {
            throw new RuntimeException("사용할 수 없는 이메일입니다.");
        }
    }

    public void deliverEmailAuthCode(String email) {
        String uuid = UUID.randomUUID().toString();
        Long emailExpiredTime = 1000L * 60 * 60 * 2;
        MemberEmail memberEmail = MemberEmail.createEmailAuthKey(email, uuid, emailExpiredTime);
        String subject = "[ESC] 이메일 인증 안내";
        String content = "<p>아래 링크를 통해 인증을 완료해주세요. </p><a href='" + emailAuthDomain
                + "'" + uuid + "> 인증 </a>";
//        mailComponents.sendMail(email,subject,content);
        memberEmailRepository.save(memberEmail);
    }

    public String emailAuthentication(String key) {
        MemberEmail memberEmail = memberEmailRepository.findById(key).orElseThrow(
                () -> new MailAuthenticationException("인증 시간이 만료되었습니다. 다시 인증해주세요."));
        memberEmail.setAuthYn(true);
        memberEmailRepository.save(memberEmail);
        return memberEmail.getId();
    }

    @Transactional
    public LoginDto.Response login(LoginDto.Request loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail()).
                orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 아이디가 없습니다."));
        checkPassword(loginDto.getPassword(), member.getPassword());
        String email = member.getEmail();
        String accessToken = jwtTokenUtil.generateAccessToken(email);
        RefreshToken refreshToken = saveRefreshToken(email);
        return LoginDto.Response.of(email, member.getImgUrl(), accessToken, refreshToken.getRefreshToken());
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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void logout(TokenDto tokenDto, String username) {
        String accessToken = resolveToken(tokenDto.getAccessToken());
        long remainMilliSeconds = jwtTokenUtil.getRemainMilliSeconds(accessToken);
        refreshTokenRedisRepository.deleteById(username);
        logoutAccessTokenRedisRepository.save(LogoutAccessToken.of(accessToken, username, remainMilliSeconds));
    }


    public TokenDto reissue(String refreshToken) {
        refreshToken = resolveToken(refreshToken);
        String username = getCurrentUsername();
        RefreshToken redisRefreshToken = refreshTokenRedisRepository.findById(username).orElseThrow(
                () -> new NoSuchElementException("인증이 만료되었습니다. 다시 로그인해주세요."));
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

    public InfoDto.Response info(UserDetails user) {
        if (user.getUsername() == null) {
            throw new UsernameNotFoundException("로그인을 해주세요,");
        }
        Member member = memberRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("다시 로그인해주세요."));
        return InfoDto.Response.builder()
                .name(member.getName())
                .email(member.getEmail())
                .password(member.getPassword())
                .imgUrl(member.getImgUrl())
                .build();
    }

    public PatchInfo.Request patchInfo(UserDetails user, PatchInfo.Request request) {
        Member member = memberRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("로그인을 해주세요."));
        if (request.getNickname() != null) {
            member.setName(request.getNickname());
        } else if (request.getImgUrl() != null) {
            member.setImgUrl(request.getImgUrl());
        }
        memberRepository.save(member);
        return request;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteMember(UserDetails user) {
        Member member = memberRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("로그인을 해주세요."));
        memberRepository.delete(member);
    }

    private String resolveToken(String token) {
        return token.substring(7);
    }

    private boolean lessThanReissueExpirationTimesLeft(String refreshToken) {
        return jwtTokenUtil.getRemainMilliSeconds(refreshToken) < JwtExpirationEnums.REISSUE_EXPIRATION_TIME.getValue();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return principal.getUsername();
    }

    public void changePasswordMail(String email) {
        String uuid = UUID.randomUUID().toString();
        Long emailExpiredTime = 1000L * 60 * 60 * 2;
        MemberEmail memberEmail = MemberEmail.createEmailAuthKey(email, uuid, emailExpiredTime);
        String subject = "[ESC] 비밀번호 변경 안내";
        String content = "<p>아래 링크를 통해 다음 단계로 진행해주세요.</p><a href='" + changePasswordEmailDomain
                + "'" + uuid + "> 인증 </a>";
        mailComponents.sendMail(email,subject,content);
        memberEmailRepository.save(memberEmail);
    }

    public String changePasswordMailAuth(String key) {
        MemberEmail memberEmail = memberEmailRepository.findById(key).orElseThrow(
                () -> new MailAuthenticationException("인증 시간이 만료되었습니다. 다시 인증해주세요."));
        memberEmail.setAuthYn(true);
        memberEmailRepository.save(memberEmail);
        return memberEmail.getId();
    }

    public void changePassword(CPasswordDto.Request request) throws AuthenticationException {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NoSuchElementException("이메일이 정확하지 않습니다."));
        boolean match = passwordEncoder.matches(request.getPrePassword(), member.getPassword());
        if(!match){
            throw new AuthenticationException("사용 중인 비밀번호가 틀렸습니다.");
        }
        if(!request.getConfirmPassword().equals(request.getNewPassword())){
            throw new RuntimeException("비밀번호와 비밀번호 확인이 같지 않습니다.");
        }
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
    }
}
