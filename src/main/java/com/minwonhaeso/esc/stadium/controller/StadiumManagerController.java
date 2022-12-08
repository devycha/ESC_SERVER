package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.security.auth.PrincipalDetails;
import com.minwonhaeso.esc.stadium.model.dto.*;
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

    @ApiOperation(value = "등록 체육관 조회", notes = "사용자(매니저)가 등록한 체육관을 조회한다.")
    @GetMapping("/manager")
    public ResponseEntity<?> getAllRegisteredStadiumsByManager(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            Pageable pageable
    ) {
        Member member = principalDetails.getMember();
        Page<StadiumResponseDto> stadiums = stadiumService.getAllStadiumsByManager(member, pageable);
        return ResponseEntity.ok().body(stadiums);
    }

    @ApiOperation(value = "체육관 신규 등록", notes = "사용자(매니저)가 체육관을 새로 등록한다.")
    @PostMapping("/register")
    public ResponseEntity<?> createStadiumByManager(
            @RequestBody StadiumDto.CreateStadiumRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
            ) {
        Member member = principalDetails.getMember();
        StadiumDto.CreateStadiumResponse stadium = stadiumService.createStadium(request, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(stadium);
    }

    @ApiOperation(value = "체육관 정보 수정", notes = "사용자(매니저)가 등록한 체육관의 정보를 수정한다.")
    @PatchMapping("/{stadiumId}/info")
    public ResponseEntity<?> updateStadiumInfo(
            @PathVariable Long stadiumId,
            StadiumDto.UpdateStadiumRequest request
    ) {
        StadiumResponseDto stadium = stadiumService.updateStadiumInfo(stadiumId, request);
        return ResponseEntity.ok().body(stadium);
    }

    @ApiOperation(value = "체육관 이미지 추가", notes = "사용자(매니저)가 등록한 체육관의 이미지를 1장 추가한다.")
    @PostMapping("/{stadiumId}/imgs")
    public ResponseEntity<?> addStadiumImgByManager(
            @PathVariable Long stadiumId,
            @RequestBody StadiumImgDto.AddImgRequest request
    ) {
        StadiumImgDto.CreateImgResponse img = stadiumService.addStadiumImg(stadiumId, request.getImgUrl());
        return ResponseEntity.ok().body(img);
    }

    @ApiOperation(value = "체육관 종목(태그) 추가", notes = "사용자(매니저)가 등록한 체육관의 종목을 1개 추가한다.")
    @PostMapping("/{stadiumId}/tags")
    public ResponseEntity<?> addStadiumTagByManager(
            @PathVariable Long stadiumId,
            @RequestBody StadiumTagDto.AddTagRequest request
    ) {
        StadiumTagDto.AddTagResponse tag = stadiumService.addStadiumTag(stadiumId, request.getTagName());
        return ResponseEntity.ok().body(tag);
    }

    @ApiOperation(value = "체육관 대여 용품 추가", notes = "사용자(매니저)가 등록한 체육관의 대여 용품을 1개 추가한다.")
    @PostMapping("/{stadiumId}/items")
    public ResponseEntity<?> addStadiumItemByManager(
            @PathVariable Long stadiumId,
            @RequestBody StadiumItemDto.CreateItemRequest request
    )  {
        StadiumItemDto.CreateItemResponse item = stadiumService.addStadiumItem(stadiumId, request);
        return ResponseEntity.ok().body(item);
    }

    @ApiOperation(value = "체육관 이미지 삭제", notes = "사용자(매니저)가 등록한 체육관의 이미지 1장을 삭제한다.")
    @DeleteMapping("/{stadiumId}/imgs")
    public ResponseEntity<?> deleteStadiumImgByManager(
            @PathVariable Long stadiumId,
            @RequestBody StadiumImgDto.DeleteImgRequest request
    ) {
        stadiumService.deleteStadiumImg(stadiumId, request.getImgUrl());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "체육관 종목(태그) 삭제", notes = "사용자(매니저)가 등록한 체육관의 종목 1개를 삭제한다.")
    @DeleteMapping("/{stadiumId}/tags")
    public ResponseEntity<?> deleteStadiumTagByManager(
            @PathVariable Long stadiumId,
            @RequestBody StadiumTagDto.DeleteTagRequest request
    ) {
        stadiumService.deleteStadiumTag(stadiumId, request.getTagName());
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "체육관 대여 용품 삭제", notes = "사용자(매니저)가 등록한 체육관의 대여 용품 1개를 삭제한다.")
    @DeleteMapping("/{stadiumId}/items")
    public ResponseEntity<?> deleteStadiumItemByManager(
            @PathVariable Long stadiumId,
            @RequestBody StadiumItemDto.DeleteItemRequest request) {
        stadiumService.deleteStadiumItem(stadiumId, request);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "체육관 삭제", notes = "사용자(매니저)가 등록한 체육관을 삭제한다.")
    @DeleteMapping("/{stadiumId}")
    public ResponseEntity<?> deleteStadiumByManager(@PathVariable Long stadiumId) {
        stadiumService.deleteStadium(stadiumId);
        return ResponseEntity.ok().build();
    }
}