package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.model.dto.StadiumLikeRequestDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumLikeResponseDto;
import com.minwonhaeso.esc.stadium.service.StadiumLikeService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@PreAuthorize("hasRole('USER')")
@RequestMapping("/stadiums")
public class StadiumLikeController {

    private final StadiumLikeService stadiumLikeService;

    @ApiOperation(value = "찜하기 or 취소", notes = "찜하기와 찜하기 취소 작업을 진행합니다.")
    @PostMapping("/{stadiumId}/likes")
    public ResponseEntity<StadiumLikeRequestDto> likes(@PathVariable(value = "stadiumId") Long stadiumId,
                                                       @AuthenticationPrincipal PrincipalDetail principalDetail){
        Member member = principalDetail.getMember();
        return ResponseEntity.ok(stadiumLikeService.likes(stadiumId, member));
    }

    @ApiOperation(value = "찜한 리스트", notes = "접속한 유저가 찜한 체육관 리스트를 보여줍니다.")
    @GetMapping("/likelist")
    public ResponseEntity<Page<StadiumLikeResponseDto>> likeList(@AuthenticationPrincipal PrincipalDetail principalDetail,
                                      Pageable pageable){
        Member member = principalDetail.getMember();
        Page<StadiumLikeResponseDto> likes = stadiumLikeService.likeList(member,pageable);
        return ResponseEntity.ok(likes);
    }

}

