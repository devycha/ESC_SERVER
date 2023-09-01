package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto;
import com.minwonhaeso.esc.stadium.service.StadiumPaymentService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@PreAuthorize("hasRole('ROLE_USER')")
@RequiredArgsConstructor
@RequestMapping("/stadiums")
public class StadiumPaymentController {
    private final StadiumPaymentService reservationService;

    @ApiOperation(value = "결제", notes = "예약 상세 정보들을 입력 받아 예약을 수행합니다.")
    @PostMapping("/{stadiumId}/payment")
    public ResponseEntity<Map<String, String>> payment(@AuthenticationPrincipal PrincipalDetail principalDetail,
                                                       @PathVariable(value = "stadiumId") Long stadiumId,
                                                       @RequestBody StadiumPaymentDto.PaymentRequest request) {
        Member member = principalDetail.getMember();
        Map<String, String> result = reservationService.payment(member, stadiumId, request);
        return ResponseEntity.ok(result);
    }

}
