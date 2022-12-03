package com.minwonhaeso.esc.member.controller;

import com.minwonhaeso.esc.member.model.dto.CPasswordDto;
import com.minwonhaeso.esc.member.model.dto.PatchInfo;
import com.minwonhaeso.esc.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/members/profiles")
@RequiredArgsConstructor
public class MemberProfileController {
    private final MemberService memberService;

    /**
     * 회원 상세 정보 요청
     **/
    @PostMapping("/info")
    public ResponseEntity<?> info(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(memberService.info(userDetails));
    }

    /**
     * 회원 수정
     **/
    @PatchMapping("/info")
    public ResponseEntity<?> patchInfo(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody PatchInfo.Request request) {
        return ResponseEntity.ok(memberService.patchInfo(userDetails, request));
    }

    /**
     * 회원 탈퇴
     **/
    @DeleteMapping("/info")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails userDetails) {
        memberService.deleteMember(userDetails);
        return ResponseEntity.ok("탈퇴에 성공했습니다.");
    }

    /**
     * 비밀번호 변경 메일 전송
     **/
    @PostMapping("/password/send-mail")
    public ResponseEntity<?> changePasswordMail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        memberService.changePasswordMail(email);
        return ResponseEntity.ok("메일이 발송되었습니다.");
    }

    /**
     * 비밀번호 변경 메일 인증코드 확인
     **/
    @GetMapping("/password")
    public ResponseEntity<?> changePasswordMailAuth(@RequestParam String key) {
        memberService.changePasswordMailAuth(key);
        return ResponseEntity.ok("메일 인증이 완료되었습니다.");
    }

    /**
     * 비밀번호 변경
     **/
    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody CPasswordDto.Request request) {
        memberService.changePassword(request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
