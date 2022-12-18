package com.minwonhaeso.esc.member.service;


import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.mail.MailService;
import com.minwonhaeso.esc.member.model.dto.*;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import com.minwonhaeso.esc.member.repository.MemberEmailRepository;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.auth.jwt.JwtExpirationEnums;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessToken;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import com.minwonhaeso.esc.util.AuthUtil;
import com.minwonhaeso.esc.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final MemberEmailRepository memberEmailRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SignDto.Response signUser(SignDto.Request signDto) {
        Optional<MemberEmail> memberEmail = memberEmailRepository.findById(signDto.getKey());
        if (memberEmail.isEmpty()) {
            throw new AuthException(AuthErrorCode.EmailAuthNotYet);
        }
        memberEmailRepository.delete(memberEmail.get());
        signDto.setPassword(passwordEncoder.encode(signDto.getPassword()));
        Member member = Member.of(signDto);
        memberRepository.save(member);
        return SignDto.Response.builder()
                .nickname(member.getNickname())
                .name(member.getName())
                .image(member.getImgUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, String> emailDuplicateYn(String email) {
        Optional<Member> optional = memberRepository.findByEmail(email);
        if (optional.isPresent()) {
            throw new AuthException(AuthErrorCode.EmailAlreadySignUp);
        }
        return successMessage("사용 가능한 이메일입니다.");
    }

    public String deliverEmailAuthCode(String email) {
        String uuid = AuthUtil.generateEmailAuthNum();
        Long emailExpiredTime = 1000L * 60 * 60 * 2;
        MemberEmail memberEmail = MemberEmail.createEmailAuthKey(email, uuid, emailExpiredTime);
        String subject = "[ESC] 이메일 인증 안내";
        String content = "<p>이메일 인증 코드 : " + uuid + "</p>";
        mailService.sendMail(email, subject, content);
        memberEmailRepository.save(memberEmail);
        return memberEmail.getId();
    }

    public Map<String, String> emailAuthentication(String key) {
        MemberEmail memberEmail = memberEmailRepository.findById(key).orElseThrow(
                () -> new AuthException(AuthErrorCode.EmailAuthTimeOut));
        memberEmailRepository.save(memberEmail);
        if (!memberEmail.getId().equals(key)) {
            throw new AuthException(AuthErrorCode.AuthKeyNotMatch);
        }
        return successMessage("메일 인증이 완료되었습니다.");
    }

    @Transactional
    public LoginDto.Response login(LoginDto.Request loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail()).
                orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotFound));
        checkPassword(loginDto.getPassword(), member.getPassword());
        String email = member.getEmail();
        String accessToken = jwtTokenUtil.generateAccessToken(email);
        RefreshToken refreshToken = jwtTokenUtil.saveRefreshToken(email);
        return LoginDto.Response.of(email,member.getNickname(), member.getImgUrl(), accessToken, refreshToken.getRefreshToken());
    }

    private void checkPassword(String rawPassword, String findMemberPassword) {
        if (!passwordEncoder.matches(rawPassword, findMemberPassword)) {
            throw new AuthException(AuthErrorCode.PasswordNotEqual);
        }
    }

    public Map<String, String> logout(TokenDto tokenDto, String username) {
        String accessToken = resolveToken(tokenDto.getAccessToken());
        long remainMilliSeconds = jwtTokenUtil.getRemainMilliSeconds(accessToken);
        refreshTokenRedisRepository.deleteById(username);
        logoutAccessTokenRedisRepository.save(LogoutAccessToken.of(accessToken, username, remainMilliSeconds));
        return successMessage("로그아웃");
    }


    public TokenDto reissue(String refreshToken) {
        refreshToken = resolveToken(refreshToken);
        String username = getCurrentUsername();
        RefreshToken redisRefreshToken = refreshTokenRedisRepository.findById(username).orElseThrow(
                () -> new AuthException(AuthErrorCode.AccessTokenAlreadyExpired));
        if (refreshToken.equals(redisRefreshToken.getRefreshToken())) {
            return reissueRefreshToken(refreshToken, username);
        }
        throw new AuthException(AuthErrorCode.TokenNotMatch);
    }

    private TokenDto reissueRefreshToken(String refreshToken, String username) {
        if (lessThanReissueExpirationTimesLeft(refreshToken)) {
            String accessToken = jwtTokenUtil.generateAccessToken(username);
            return TokenDto.of(accessToken, jwtTokenUtil.saveRefreshToken(username).getRefreshToken());
        }
        return TokenDto.of(jwtTokenUtil.generateAccessToken(username), refreshToken);
    }

    public InfoDto.Response info(UserDetails user) {
        if (user.getUsername() == null) {
            throw new AuthException(AuthErrorCode.MemberNotLogIn);
        }
        Member member = memberRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotLogIn));
        return InfoDto.Response.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .imgUrl(member.getImgUrl())
                .name(member.getName())
                .password(member.getPassword())
                .build();
    }

    public PatchInfo.Request patchInfo(UserDetails user, PatchInfo.Request request) {
        Member member = memberRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotLogIn));
        if (request.getNickname() != null) {
            member.setNickname(request.getNickname());
        }

        if (request.getImgUrl() != null) {
            member.setImgUrl(request.getImgUrl());
        }
        memberRepository.save(member);
        return request;
    }

    public Map<String, String> deleteMember(UserDetails user) {
        Member member = memberRepository.findByEmail(user.getUsername())
                .orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotLogIn));
        memberRepository.delete(member);
        return successMessage("탈퇴에 성공했습니다.");
    }

    public String resolveToken(String token) {
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

    public String changePasswordMail(String email) {
        String uuid = AuthUtil.generateEmailAuthNum();
        Long emailExpiredTime = 1000L * 60 * 60 * 2;
        MemberEmail memberEmail = MemberEmail.createEmailAuthKey(email, uuid, emailExpiredTime);
        String subject = "[ESC] 비밀번호 변경 안내";
        String content = "<p>비밀번호 변경 코드: " + uuid + "</p>";
//        mailService.sendMail(email, subject, content);
        memberEmailRepository.save(memberEmail);
        return uuid;
    }

    public Map<String, String> changePasswordMailAuth(String key) {
        MemberEmail memberEmail = memberEmailRepository.findById(key).orElseThrow(
                () -> new AuthException(AuthErrorCode.EmailAuthTimeOut));
        memberEmailRepository.delete(memberEmail);
        if (!memberEmail.getId().equals(key)) {
            throw new AuthException(AuthErrorCode.AuthKeyNotMatch);
        }
        return successMessage("메일 인증이 완료되었습니다.");
    }

    public Map<String, String> changePassword(CPasswordDto.Request request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.EmailNotMatched));

        if(request.getHasToken()) {
            boolean match = passwordEncoder.matches(request.getPrePassword(), member.getPassword());

            if (!match) {
                throw new AuthException(AuthErrorCode.PasswordNotEqual);
            }

            if (!request.getConfirmPassword().equals(request.getNewPassword())) {
                throw new AuthException(AuthErrorCode.PasswordNotEqual);
            }

            member.setPassword(passwordEncoder.encode(request.getNewPassword()));

        }else{

            if(!request.getNewPassword().equals(request.getConfirmPassword())){
                throw new AuthException(AuthErrorCode.PasswordNotEqual);
            }

            String password = passwordEncoder.encode(request.getNewPassword());
            member.setPassword(password);
        }

        memberRepository.save(member);
        return successMessage("비밀번호가 성공적으로 변경되었습니다.");
    }

    public Map<String, String> successMessage(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }

    public OAuthDto.Response oauthInfo(OAuthDto.Request oauthDto) {
        Member member = memberRepository.findByEmail(oauthDto.getEmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.MemberNotFound));

        String email = member.getEmail();
        RefreshToken refreshToken = jwtTokenUtil.saveRefreshToken(email);

        return OAuthDto.Response.builder()
                .nickname(member.getNickname())
                .imgUrl(member.getImgUrl())
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }
}
