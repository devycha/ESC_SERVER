package com.minwonhaeso.esc.member.controller;

import com.minwonhaeso.esc.member.model.dto.LoginDto;
import com.minwonhaeso.esc.member.model.dto.OAuthDto;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.dto.TokenDto;
import com.minwonhaeso.esc.member.service.MemberService;
import com.minwonhaeso.esc.util.JwtTokenUtil;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation(value = "회원 가입", notes = "입력한 정보로 회원가입한다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignDto.Request signDto) {
        SignDto.Response response = memberService.signUser(signDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원가입 이메일 중복 검사
     **/
    @ApiOperation(value = "이메일 중복 확인", notes = "입력 받은 이메일의 회원이 이미 존재하는지 확인한다.")
    @PostMapping("/email-dup")
    public ResponseEntity<?> emailDuplicated(@RequestBody Map<String, String> email) {
        Map<String, String> result = memberService.emailDuplicateYn(email.get("email"));
        return ResponseEntity.ok(result);
    }

    /**
     * 회원가입 이메일 인증 코드 전송
     **/
    @ApiOperation(value = "이메일 인증 코드 전송", notes = "이메일로 회원가입을 위한 코드를 전송합니다.")
    @PostMapping("/email-auth")
    public ResponseEntity<?> deliverEmailAuthCode(@RequestBody Map<String, String> email) {
        memberService.deliverEmailAuthCode(email.get("email"));
        Map<String, String> result = memberService.successMessage("이메일 인증 코드를 전송했습니다.");
        return ResponseEntity.ok(result);
    }

    /**
     * 메일 인증
     **/
    @ApiOperation(value = "메일 인증", notes = "메일 인증 코드가 맞는지 확인합니다.")
    @GetMapping("/email-authentication")
    public ResponseEntity<?> emailAuthentication(@RequestParam String key) {
        Map<String, String> result = memberService.emailAuthentication(key);
        return ResponseEntity.ok(result);
    }

    /**
     * 로그인
     **/
    @ApiOperation(value = "로그인", notes = "일반 계정 혹은 판매자로 사용자가 로그인합니다.")
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDto.Request loginDto) {
        LoginDto.Response response = memberService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     **/
    @ApiOperation(value = "로그아웃", notes = "사용자의 로그아웃")
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken) {
        String username = jwtTokenUtil.getUsername(resolveToken(accessToken));
        Map<String, String> result = memberService.logout(TokenDto.of(accessToken), username);
        return ResponseEntity.ok(result);
    }

    /**
     * Token reissue
     **/
    @ApiOperation(value = "토큰 재발급", notes = "유효기간이 지난 토큰을 재발급합니다.")
    @PostMapping("/auth/refresh-token")
    public ResponseEntity<?> reissue(@RequestHeader("RefreshToken") String refreshToken) {
        return ResponseEntity.ok(memberService.reissue(refreshToken));
    }

    @ApiOperation(value = "OAuth2 회원 정보 요청", notes = "소셜 로그인 시, 필요한 회원 정보를 전달합니다.")
    @PostMapping("/oauth2/info")
    public ResponseEntity<?> oauth2Info(@RequestBody OAuthDto.Request oauthDto) {
        OAuthDto.Response response = memberService.oauthInfo(oauthDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Bearer 부분 빼는 method
     **/
    private String resolveToken(String accessToken) {
        return accessToken.substring(7);
    }

}
