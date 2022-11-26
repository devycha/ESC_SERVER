package com.minwonhaeso.esc.member.service;


import com.minwonhaeso.esc.component.MailComponents;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.entity.MemberEmail;
import com.minwonhaeso.esc.member.model.type.EmailAuthStatus;
import com.minwonhaeso.esc.member.repository.MemberEmailRepository;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemberService {
    @Value("${spring.mail.domain}")
    private String domain;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, MailComponents mailComponents, MemberEmailRepository memberEmailRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailComponents = mailComponents;
        this.memberEmailRepository = memberEmailRepository;
    }

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailComponents mailComponents;
    private final MemberEmailRepository memberEmailRepository;
//    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
//    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
//    private final JwtTokenUtil jwtTokenUtil;

    public void signUser(SignDto signDto) {
        signDto.setPassword(passwordEncoder.encode(signDto.getPassword()));
        Member member =  Member.of(signDto);
        MemberEmail memberEmail = memberEmailRepository
                .findByEmail(member.getEmail()).orElseThrow(() -> new MailAuthenticationException("메일 인증을 먼저 해주세요."));
        if(memberEmail.getStatus() != EmailAuthStatus.VERIFICATION_COMPLETE){
            throw new MailAuthenticationException("메일 인증을 먼저 진행해주세요.");
        }
        member.setMemberEmail(memberEmail);
        memberRepository.save(member);
    }

    public void emailDuplicateYn(String email) {
        Optional<Member> optional = memberRepository.findByEmail(email);
        if(!optional.isEmpty()){
            throw new RuntimeException("사용할 수 없는 이메일입니다.");
        }
    }

    public void deliverEmailAuthCode(String email) {
        String uuid = UUID.randomUUID().toString();
        MemberEmail memberEmail = MemberEmail.builder()
                .email(email)
                .authKey(uuid)
                .expireDt(LocalDateTime.now().plusHours(2))
                .status(EmailAuthStatus.VERIFICATION_ING)
                .build();
        String subject = "[ESC] 이메일 인증 안내";
        String content = "<p>아래 링크를 통해 인증을 완료해주세요. </p><a href='"+ domain
                + "'" + uuid + "> 인증 </a>";
        mailComponents.sendMail(email,subject,content);
        memberEmailRepository.save(memberEmail);


    }

    public void emailAuthentication(String key) {
        MemberEmail memberEmail = memberEmailRepository.findByAuthKey(key).orElseThrow(
                () -> new MailAuthenticationException("메일 인증에 실패하셨습니다. 인증 코드를 다시 받으세요."));
        if(memberEmail.getExpireDt().isBefore(LocalDateTime.now())){
            throw new MailAuthenticationException("인증코드 유효 시간이 끝났습니다.");
        }
        memberEmail.setValidateDt(LocalDateTime.now());
        memberEmail.setStatus(EmailAuthStatus.VERIFICATION_COMPLETE);
        memberEmailRepository.save(memberEmail);

    }
}
