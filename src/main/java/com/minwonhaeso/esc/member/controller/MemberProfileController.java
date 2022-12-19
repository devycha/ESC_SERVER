package com.minwonhaeso.esc.member.controller;

import com.minwonhaeso.esc.member.model.dto.CPasswordDto;
import com.minwonhaeso.esc.member.model.dto.PatchInfo;
import com.minwonhaeso.esc.member.service.MemberService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/members/profiles")
@RequiredArgsConstructor
public class MemberProfileController {
    private final MemberService memberService;

    /**
     * 회원 상세 정보 요청
     **/
    @ApiOperation(value = "회원 상세 정보", notes = "회원 정보 페이지에 필요한 상세 정보를 보여줍니다.")
    @PostMapping("/info")
    public ResponseEntity<?> info(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(memberService.info(userDetails));
    }

    /**
     * 회원 수정
     **/
    @ApiOperation(value = "회원 정보 수정", notes = "프로필 사진과 닉네임 정보를 수정합니다.")
    @PatchMapping("/info")
    public ResponseEntity<?> patchInfo(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody PatchInfo.Request request) {
        return ResponseEntity.ok(memberService.patchInfo(userDetails, request));
    }

    /**
     * 회원 탈퇴
     **/
    @ApiOperation(value = "회원 탈퇴", notes = "해당 서비스를 탈퇴합니다.")
    @DeleteMapping("/info")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> result = memberService.deleteMember(userDetails);
        return ResponseEntity.ok(result);
    }

    /**
     * 비밀번호 변경 메일 전송
     **/
    @ApiOperation(value = "비밀번호 변경 이메일 전송", notes = "인증 코드와 함께 본인확인을 위한 이메일을 전송한다.")
    @PostMapping("/password/send-email")
    public ResponseEntity<?> changePasswordMail(@RequestBody Map<String, String> body) {
        memberService.changePasswordMail(body.get("email"));
        Map<String, String> result = memberService.successMessage("메일이 발송되었습니다.");
        return ResponseEntity.ok(result);
    }

    /**
     * 비밀번호 변경 메일 인증코드 확인
     **/
    @ApiOperation(value = "인증코드 확인(비밀번호 변경)", notes = "비밀번호 변경을 위한 메일 인증 코드 일치 여부를 확인합니다.")
    @PostMapping("/password/config")
    public ResponseEntity<?> changePasswordMailAuth(@RequestBody Map<String, String> response) {
        String key = response.get("key");
        Map<String, String> result = memberService.changePasswordMailAuth(key);
        return ResponseEntity.ok(result);
    }

    /**
     * 비밀번호 변경
     **/
    @ApiOperation(value = "비밀번호 변경", notes = "이전 비밀번호가 일치하는지 확인하고, 새로운 비밀번호로 변경합니다.")
    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody CPasswordDto.Request request) {
        Map<String, String> result = memberService.changePassword(request);
        return ResponseEntity.ok(result);
    }
}
