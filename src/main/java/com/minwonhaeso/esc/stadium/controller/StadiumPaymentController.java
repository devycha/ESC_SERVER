package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import com.minwonhaeso.esc.stadium.service.StadiumPaymentService;
import com.minwonhaeso.esc.stadium.service.StadiumReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.minwonhaeso.esc.stadium.model.dto.StadiumPaymentDto.*;

@RestController
@PreAuthorize("hasRole('ROLE_USER')")
@RequiredArgsConstructor
@RequestMapping("/stadiums")
public class StadiumPaymentController {
    private final StadiumPaymentService reservationService;


    @GetMapping("/{stadiumId}/payment")
    public ResponseEntity<?> paymentPage(@AuthenticationPrincipal PrincipalDetail principalDetail,
                                         @PathVariable(value = "stadiumId") Long stadiumId,
                                         @RequestBody PaymentConfirmRequest request){
        Member member = principalDetail.getMember();
        PaymentConfirmResponse response = reservationService.paymentConfirm(request,member);
        return ResponseEntity.ok(response);
    }

}
