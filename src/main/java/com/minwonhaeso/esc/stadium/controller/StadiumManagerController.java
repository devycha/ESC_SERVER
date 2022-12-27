package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.model.dto.*;
import com.minwonhaeso.esc.stadium.model.dto.StadiumDto.CreateStadiumResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumItemDto.CreateItemResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.ReservationInfoResponse;
import com.minwonhaeso.esc.stadium.model.dto.StadiumReservationDto.StadiumReservationUserResponse;
import com.minwonhaeso.esc.stadium.service.StadiumReservationService;
import com.minwonhaeso.esc.stadium.service.StadiumService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ROLE_STADIUM')")
@RequiredArgsConstructor
@RestController
@RequestMapping("/stadiums")
public class StadiumManagerController {
    private final StadiumService stadiumService;
    private final StadiumReservationService stadiumReservationService;

    @ApiOperation(value = "등록 체육관 조회", notes = "사용자(매니저)가 등록한 체육관을 조회한다.")
    @GetMapping("/manager")
    public ResponseEntity<Page<StadiumResponseDto>> getAllRegisteredStadiumsByManager(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            Pageable pageable
    ) {
        Member member = principalDetail.getMember();
        Page<StadiumResponseDto> stadiums = stadiumService.getAllStadiumsByManager(member, pageable);
        return ResponseEntity.ok().body(stadiums);
    }

    @ApiOperation(value = "체육관 누적 사용자 조회", notes = "사용자(매니저)가 등록한 체육관의 누적 사용자를 조회한다.")
    @GetMapping("/manager/{stadiumId}")
    public ResponseEntity<Page<StadiumReservationUserResponse>> getAllReservationUsers(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            Pageable pageable
    ) {
        Member member = principalDetail.getMember();
        Page<StadiumReservationUserResponse> reservationList = stadiumReservationService
                .getAllReservationUsersByManager(member, stadiumId, pageable);
        return ResponseEntity.ok().body(reservationList);
    }

    @ApiOperation(value = "체육관 사용자 상세 정보 조회", notes = "")
    @GetMapping("/manager/{stadiumId}/reservations/{reservationId}")
    public ResponseEntity<?> getStadiumReservationInfo(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @PathVariable Long reservationId
    ) {
        Member member = principalDetail.getMember();
        ReservationInfoResponse reservation = stadiumReservationService
                .getReservationInfoByManager(member, stadiumId, reservationId);
        return ResponseEntity.ok().body(reservation);
    }

    @ApiOperation(value = "체육관 신규 등록", notes = "사용자(매니저)가 체육관을 새로 등록한다.")
    @PostMapping("/register")
    public ResponseEntity<CreateStadiumResponse> createStadiumByManager(
            @RequestBody StadiumDto.CreateStadiumRequest request,
            @AuthenticationPrincipal PrincipalDetail principalDetail
            ) {
        Member member = principalDetail.getMember();
        CreateStadiumResponse stadium = stadiumService.createStadium(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(stadium);
    }

    @ApiOperation(value = "체육관 정보 수정", notes = "사용자(매니저)가 등록한 체육관의 정보를 수정한다.")
    @PatchMapping("/{stadiumId}/info")
    public ResponseEntity<StadiumInfoResponseDto> updateStadiumInfo(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody StadiumDto.UpdateStadiumRequest request
    ) {
        Member member = principalDetail.getMember();
        StadiumInfoResponseDto stadium = stadiumService.updateStadiumInfo(member, stadiumId, request);
        return ResponseEntity.ok().body(stadium);
    }

    @ApiOperation(value = "체육관 이미지 추가", notes = "사용자(매니저)가 등록한 체육관의 이미지를 1장 추가한다.")
    @PostMapping("/{stadiumId}/imgs")
    public ResponseEntity<StadiumImgDto> addStadiumImgByManager(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody StadiumImgDto request
    ) {
        Member member = principalDetail.getMember();
        StadiumImgDto img = stadiumService.addStadiumImg(member, stadiumId, request.getImgUrl());
        return ResponseEntity.ok().body(img);
    }

    @ApiOperation(value = "체육관 종목(태그) 추가", notes = "사용자(매니저)가 등록한 체육관의 종목을 1개 추가한다.")
    @PostMapping("/{stadiumId}/tags")
    public ResponseEntity<StadiumTagDto> addStadiumTagByManager(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody StadiumTagDto request
    ) {
        Member member = principalDetail.getMember();
        StadiumTagDto tag = stadiumService.addStadiumTag(member, stadiumId, request.getTagName());
        return ResponseEntity.ok().body(tag);
    }

    @ApiOperation(value = "체육관 대여 용품 추가", notes = "사용자(매니저)가 등록한 체육관의 대여 용품을 1개 추가한다.")
    @PostMapping("/{stadiumId}/items")
    public ResponseEntity<CreateItemResponse> addStadiumItemByManager(
            @PathVariable Long stadiumId,
            @RequestBody StadiumItemDto.CreateItemRequest request
    )  {
        CreateItemResponse item = stadiumService.addStadiumItem(stadiumId, request);
        return ResponseEntity.ok().body(item);
    }

    @ApiOperation(value = "체육관 이미지 삭제", notes = "사용자(매니저)가 등록한 체육관의 이미지 1장을 삭제한다.")
    @DeleteMapping("/{stadiumId}/imgs")
    public ResponseEntity<?> deleteStadiumImgByManager(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody StadiumImgDto request
    ) {
        Member member = principalDetail.getMember();
        stadiumService.deleteStadiumImg(member, stadiumId, request.getImgUrl());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "체육관 종목(태그) 삭제", notes = "사용자(매니저)가 등록한 체육관의 종목 1개를 삭제한다.")
    @DeleteMapping("/{stadiumId}/tags")
    public ResponseEntity<?> deleteStadiumTagByManager(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody StadiumTagDto request
    ) {
        Member member = principalDetail.getMember();
        stadiumService.deleteStadiumTag(member, stadiumId, request.getTagName());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "체육관 대여 용품 삭제", notes = "사용자(매니저)가 등록한 체육관의 대여 용품 1개를 삭제한다.")
    @DeleteMapping("/{stadiumId}/items")
    public ResponseEntity<?> deleteStadiumItemByManager(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody StadiumItemDto.DeleteItemRequest request) {
        Member member = principalDetail.getMember();
        stadiumService.deleteStadiumItem(member, stadiumId, request);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "체육관 삭제", notes = "사용자(매니저)가 등록한 체육관을 삭제한다.")
    @DeleteMapping("/{stadiumId}/info")
    public ResponseEntity<?> deleteStadiumByManager(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId) {
        Member member = principalDetail.getMember();
        stadiumService.deleteStadium(member, stadiumId);
        return ResponseEntity.ok().build();
    }
}