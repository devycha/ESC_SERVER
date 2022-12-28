package com.minwonhaeso.esc.member.service;


import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.mail.MailService;
import com.minwonhaeso.esc.member.model.dto.*;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.member.repository.redis.MemberEmailRepository;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.auth.jwt.JwtExpirationEnums;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessToken;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumReservation;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
import com.minwonhaeso.esc.stadium.model.type.StadiumStatus;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import com.minwonhaeso.esc.util.AuthUtil;
import com.minwonhaeso.esc.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.minwonhaeso.esc.error.type.AuthErrorCode.*;
import static com.minwonhaeso.esc.error.type.StadiumErrorCode.*;
import static com.minwonhaeso.esc.member.model.type.MemberStatus.*;
import static com.minwonhaeso.esc.member.model.type.MemberType.*;
import static com.minwonhaeso.esc.member.model.type.MemberType.MANAGER;

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
    private final StadiumRepository stadiumRepository;
    private final StadiumReservationRepository stadiumReservationRepository;

    @Transactional( isolation = Isolation.SERIALIZABLE)
    public SignDto.Response signUser(SignDto.Request signDto) {
        MemberEmail memberEmail = memberEmailRepository.findById(signDto.getKey()).orElseThrow(
                ()-> new AuthException(AuthKeyNotMatch));
        if(!memberEmail.getEmail().equals(signDto.getEmail())){
            throw new AuthException(AuthKeyNotMatch);
        }
        signDto.setPassword(passwordEncoder.encode(signDto.getPassword()));
        Member member = Member.of(signDto);
        try {
            memberEmailRepository.delete(memberEmail);

            memberRepository.save(member);
        }catch(Exception e){
            memberEmailRepository.save(memberEmail);
        }
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
            throw new AuthException(EmailAlreadySignUp);
        }
        return successMessage("사용 가능한 이메일입니다.");
    }

    public void deliverEmailAuthCode(String email) {
        String uuid = AuthUtil.generateEmailAuthNum();
        Long emailExpiredTime = 1000L * 60 * 60 * 2;
        MemberEmail memberEmail = MemberEmail.createEmailAuthKey(email, uuid, emailExpiredTime);
        String subject = "[ESC] 이메일 인증 안내";
        String content = "<p>이메일 인증 코드 : " + uuid + "</p>";
        mailService.sendMail(email, subject, content);
        memberEmailRepository.save(memberEmail);
    }

    public Map<String, String> emailAuthentication(String key) {
        memberEmailRepository.findById(key).orElseThrow(
                () -> new AuthException(AuthKeyNotMatch));
        return successMessage("메일 인증이 완료되었습니다.");
    }

    @Transactional
    public LoginDto.Response login(LoginDto.Request loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail()).
                orElseThrow(() -> new AuthException(MemberNotFound));
        checkPassword(loginDto.getPassword(), member.getPassword());
        if(!Objects.equals(member.getType().name(), loginDto.getType())){
            throw new AuthException(MemberTypeNotMatch);
        }
        String email = member.getEmail();
        String accessToken = jwtTokenUtil.generateAccessToken(email);
        RefreshToken refreshToken = jwtTokenUtil.saveRefreshToken(email);
        return LoginDto.Response.of(member.getMemberId(), email, member.getNickname(), member.getImgUrl(), accessToken,
                refreshToken.getRefreshToken(),member.getType().name());
    }

    private void checkPassword(String rawPassword, String findMemberPassword) {
        if (!passwordEncoder.matches(rawPassword, findMemberPassword)) {
            throw new AuthException(PasswordNotEqual);
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
        String username = getCurrentUsernameInRefreshToken(refreshToken);
        RefreshToken redisRefreshToken = refreshTokenRedisRepository.findById(username).orElseThrow(
                () -> new AuthException(AccessTokenAlreadyExpired));
        if (refreshToken.equals(redisRefreshToken.getRefreshToken())) {
            return reissueRefreshToken(refreshToken, username);
        }
        throw new AuthException(TokenNotMatch);
    }

    private TokenDto reissueRefreshToken(String refreshToken, String username) {
        if (lessThanReissueExpirationTimesLeft(refreshToken)) {
            String accessToken = jwtTokenUtil.generateAccessToken(username);
            return TokenDto.of(accessToken, jwtTokenUtil.saveRefreshToken(username).getRefreshToken());
        }
        return TokenDto.of(jwtTokenUtil.generateAccessToken(username), refreshToken);
    }

    public InfoDto.Response info(Member member) {
        return InfoDto.Response.builder()
                .id(member.getMemberId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .imgUrl(member.getImgUrl())
                .name(member.getName())
                .build();
    }

    public PatchInfo.Request patchInfo(Member member, PatchInfo.Request request) {
        if (request.getNickname() != null) {
            member.setNickname(request.getNickname());
        }

        if (request.getImgUrl() != null) {
            member.setImgUrl(request.getImgUrl());
        }
        memberRepository.save(member);
        return request;
    }

    public Map<String, String> deleteMember(Member member) {
        MemberType type = member.getType();
        if(member.getStatus() != STOP) {
            if (type == MANAGER) {
                List<Stadium> stadiums = stadiumRepository.findAllByMember(member);
                for (Stadium stadium : stadiums) {
                    if (stadium.getReservations().size() != 0) {
                        throw new StadiumException(HasReservation);
                    }
                    stadium.setStatus(StadiumStatus.DELETED);
                }
                stadiumRepository.saveAll(stadiums);
            }
            if (type == USER) {
                List<StadiumReservation> reservations =
                        stadiumReservationRepository.findALlByMember(member)
                        .stream().filter(stadiumReservation
                                    -> stadiumReservation.getStatus() != StadiumReservationStatus.RESERVED)
                        .collect(Collectors.toList());

                if (reservations.size() != 0) {
                    throw new StadiumException(HasReservation);
                }
                member.setStatus(STOP);
            }
            memberRepository.save(member);
            return successMessage("탈퇴에 성공했습니다.");
        }else throw new AuthException(AlreadyStopMember);
    }

    public String resolveToken(String token) {
        return token.substring(7);
    }

    private boolean lessThanReissueExpirationTimesLeft(String refreshToken) {
        return jwtTokenUtil.getRemainMilliSeconds(refreshToken) < JwtExpirationEnums.REISSUE_EXPIRATION_TIME.getValue();
    }


    private String getCurrentUsernameInRefreshToken(String refreshToken) {
        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new AuthException(AccessTokenAlreadyExpired);
        }

        return jwtTokenUtil.getUsername(refreshToken);
    }

    public void changePasswordMail(String email) {
        String uuid = AuthUtil.generateEmailAuthNum();
        Long emailExpiredTime = 1000L * 60 * 60 * 2;
        MemberEmail memberEmail = MemberEmail.createEmailAuthKey(email, uuid, emailExpiredTime);
        String subject = "[ESC] 비밀번호 변경 안내";
        String content = "<p>비밀번호 변경 코드: " + uuid + "</p>";
        mailService.sendMail(email, subject, content);
        memberEmailRepository.save(memberEmail);
    }

    public Map<String, String> changePasswordMailAuth(String key) {
        MemberEmail memberEmail = memberEmailRepository.findById(key).orElseThrow(
                () -> new AuthException(EmailAuthTimeOut));
        if (!memberEmail.getId().equals(key)) {
            throw new AuthException(AuthKeyNotMatch);
        }
        memberEmailRepository.delete(memberEmail);
        return successMessage("메일 인증이 완료되었습니다.");
    }

    public Map<String, String> changePassword(CPasswordDto.Request request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException(EmailNotMatched));

        if(request.getHasToken()) {
            boolean match = passwordEncoder.matches(request.getPrePassword(), member.getPassword());

            if (!match) {
                throw new AuthException(PasswordNotEqual);
            }

            if (!request.getConfirmPassword().equals(request.getNewPassword())) {
                throw new AuthException(PasswordNotEqual);
            }

            member.setPassword(passwordEncoder.encode(request.getNewPassword()));

        }else{

            if(!request.getNewPassword().equals(request.getConfirmPassword())){
                throw new AuthException(PasswordNotEqual);
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
                .orElseThrow(() -> new AuthException(MemberNotFound));

        String email = member.getEmail();
        RefreshToken refreshToken = jwtTokenUtil.saveRefreshToken(email);

        return OAuthDto.Response.builder()
                .id(member.getMemberId())
                .nickname(member.getNickname())
                .imgUrl(member.getImgUrl())
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }
}
