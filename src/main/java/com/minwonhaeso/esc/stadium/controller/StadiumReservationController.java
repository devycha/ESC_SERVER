package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.notification.model.type.NotificationType;
import com.minwonhaeso.esc.notification.service.NotificationService;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.CreateReservationRequest;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.PriceResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.ReservationInfoResponse;
import com.minwonhaeso.esc.stadium.service.StadiumReservationService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@PreAuthorize("hasRole('ROLE_USER')")
@RequiredArgsConstructor
@RestController
@RequestMapping("/stadiums")
public class StadiumReservationController {
    private final StadiumReservationService stadiumReservationService;
    private final NotificationService notificationService;

    @ApiOperation(value = "내 예약 목록", notes = "내가 예약한 목록 모두 조회")
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

    @ApiOperation(value = "체육관 예약 페이지", notes = "특정 체육관 예약 페이지 정보 요청")
    @GetMapping("/{stadiumId}/reservation")
    public ResponseEntity<?> getStadiumReservationInfo(
            @PathVariable Long stadiumId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
            ) {

        if (date == null) {
            date = LocalDate.now();
        }

        ReservationInfoResponse reservationInfo =
                stadiumReservationService.getStadiumReservationInfo(stadiumId, date);
        return ResponseEntity.ok().body(reservationInfo);
    }

    @ApiOperation(value = "체육관 예약 상세 내역 조회", notes = "특정 체육관의 특정 예약 내역 조회")
    @GetMapping("/{stadiumId}/reservations/{reservationId}")
    public ResponseEntity<?> getReservationInfo(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @PathVariable Long reservationId
    ) {
        Member member = principalDetail.getMember();
        ReservationInfoResponse reservationInfo =
                stadiumReservationService.getReservationInfo(member, stadiumId, reservationId);
        return ResponseEntity.ok().body(reservationInfo);
    }

    @GetMapping("/{stadiumId}/reservation/price")
    public ResponseEntity<?> getStadiumReservationPrice(
            @PathVariable Long stadiumId,
            @RequestParam LocalDate date,
            @RequestBody CreateReservationRequest request
    ) {
        PriceResponse price = stadiumReservationService.getPrice(stadiumId, date, request);
        return ResponseEntity.ok().body(price);
    }

    @PostMapping("/{stadiumId}/reservation")
    public ResponseEntity<?> createReservation(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody CreateReservationRequest request

    ) {
        Member member = principalDetail.getMember();
        ReservationInfoResponse reservationInfo =
                stadiumReservationService.createReservation(member, stadiumId, request);
        notificationService.createNotification(
                NotificationType.RESERVATION, stadiumId, reservationInfo.getId(),
                "새로운 예약이 있습니다.", member);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationInfo);
    }

    @DeleteMapping("/{stadiumId}/reservations/{reservationId}")
    public ResponseEntity<?> deleteReservation(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @PathVariable Long reservationId
    ) {
        Member member = principalDetail.getMember();
        stadiumReservationService.deleteReservation(member, stadiumId, reservationId);
        return ResponseEntity.ok().build();
    }
}
