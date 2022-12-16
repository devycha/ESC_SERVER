package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.stadium.model.dto.StadiumLikeResponseDto;
import com.minwonhaeso.esc.stadium.service.StadiumLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@PreAuthorize("hasRole('USER')")
@RequestMapping("/stadiums")
public class StadiumLikeController {

    private final StadiumLikeService stadiumLikeService;

    @PostMapping("/{stadiumId}/likes/{type}")
    public ResponseEntity<?> likes(@PathVariable(value = "stadiumId") Long stadiumId,
                                   @PathVariable(value = "type") String type,
                                   @AuthenticationPrincipal PrincipalDetail principalDetail){
        Member member = principalDetail.getMember();
        Map<String,String> result =  stadiumLikeService.likes(stadiumId,type,member);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/likelist")
    public ResponseEntity<?> likeList(@AuthenticationPrincipal PrincipalDetail principalDetail,
                                      Pageable pageable){
        Member member = principalDetail.getMember();
        Page<StadiumLikeResponseDto> likes = stadiumLikeService.likeList(member,pageable);
        return ResponseEntity.ok(likes);
    }

}

