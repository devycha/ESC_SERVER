package com.minwonhaeso.esc.member.controller;

import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /** 회원가입**/
    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody SignDto signDto){
        memberService.signUser(signDto);
        return ResponseEntity.ok("회원가입에 성공하셨습니다.");
    }
    /** 회원가입 이메일 중복 검사**/
    @PostMapping("/email-dup")
    public ResponseEntity<?> emailDuplicated(@RequestBody Map<String, String> email){
        memberService.emailDuplicateYn(email.get("email"));
        return ResponseEntity.ok("사용 가능한 이메일 입니다.");
    }
    /** 회원가입 이메일 인증 코드 전송 **/
    @PostMapping("/email-auth")
    public ResponseEntity<?> deliverEmailAuthCode(@RequestBody Map<String, String> email){
        memberService.deliverEmailAuthCode(email.get("email"));

        return ResponseEntity.ok("이메일 인증 코드를 전송했습니다.");
    }
    @PostMapping("/email-authentication")
    public ResponseEntity<?> emailAuthentication(@RequestBody Map<String, String> key){
        memberService.emailAuthentication(key.get("key"));
        return ResponseEntity.ok("정상 인증 되었습니다.");
    }

}
