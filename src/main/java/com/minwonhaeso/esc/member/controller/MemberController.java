package com.minwonhaeso.esc.member.controller;

import com.minwonhaeso.esc.member.model.dto.LoginDto;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.dto.TokenDto;
import com.minwonhaeso.esc.member.service.MemberService;
import com.minwonhaeso.esc.security.auth.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 회원가입
     **/
    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody SignDto.Request signDto) {
        SignDto.Response response = memberService.signUser(signDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원가입 이메일 중복 검사
     **/
    @PostMapping("/email-dup")
    public ResponseEntity<?> emailDuplicated(@RequestBody Map<String, String> email) {
        memberService.emailDuplicateYn(email.get("email"));
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    /**
     * 회원가입 이메일 인증 코드 전송
     **/
    @PostMapping("/email-auth")
    public ResponseEntity<?> deliverEmailAuthCode(@RequestBody Map<String, String> email) {
        memberService.deliverEmailAuthCode(email.get("email"));

        return ResponseEntity.ok("이메일 인증 코드를 전송했습니다.");
    }

    /**
     * 메일 인증
     **/
    @PostMapping("/email-authentication")
    public ResponseEntity<?> emailAuthentication(@RequestParam String key) {
        memberService.emailAuthentication(key);
        return ResponseEntity.ok("메일 인증이 완료되었습니다.");
    }

    /**
     * 로그인
     **/
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDto.Request loginDto) {
        LoginDto.Response response = memberService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     **/
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken,
                                    @RequestHeader("RefreshToken") String refreshToken) {
        String username = jwtTokenUtil.getUsername(resolveToken(accessToken));
        memberService.logout(TokenDto.of(accessToken, refreshToken), username);
        return ResponseEntity.ok("로그아웃");
    }

    /**
     * Token reissue
     **/
    @PostMapping("/auth/refresh-token")
    public ResponseEntity<?> reissue(@RequestHeader("RefreshToken") String refreshToken) {
        return ResponseEntity.ok(memberService.reissue(refreshToken));
    }


    /**
     * Bearer 부분 빼는 method
     **/
    private String resolveToken(String accessToken) {
        return accessToken.substring(7);
    }

}
