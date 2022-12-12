package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto;
import com.minwonhaeso.esc.stadium.service.StadiumReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@RequestMapping("/stadiums")
@Controller
public class StadiumReservationController {
    private final StadiumReservationService stadiumReservationService;

    @GetMapping("/reservations")
    public ResponseEntity<?> getAllReservations(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            Pageable pageable
    ) {
        Member member = principalDetail.getMember();
        Page<StadiumReservationDto.Response> reservations =
                stadiumReservationService.getAllReservationsByMember(member, pageable);
        return ResponseEntity.ok().body(reservations);
    }
}
