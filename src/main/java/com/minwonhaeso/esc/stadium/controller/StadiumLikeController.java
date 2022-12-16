package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.service.StadiumLikeService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@PreAuthorize("hasRole('USER')")
@RequestMapping("/stadiums")
public class StadiumLikeController {

    private final StadiumLikeService stadiumLikeService;

    @ApiOperation(value = "찜하기 or 취소", notes = "ON 혹은 OFF type을 받아 찜하기와 찜하기 취소 작업을 진행합니다.")
    @PostMapping("/{stadiumId}/likes")
    public ResponseEntity<?> likes(@PathVariable(value = "stadiumId") Long stadiumId,
                                   @AuthenticationPrincipal PrincipalDetail principalDetail){
        Member member = principalDetail.getMember();
        return ResponseEntity.ok(stadiumLikeService.likes(stadiumId, member));
    }
}

