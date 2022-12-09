package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.stadium.model.dto.SearchStadiumDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumInfoResponseDto;
import com.minwonhaeso.esc.stadium.model.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.model.entity.StadiumDocument;
import com.minwonhaeso.esc.stadium.service.StadiumSearchService;
import com.minwonhaeso.esc.stadium.service.StadiumService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stadiums")
public class StadiumUserController {
    private final StadiumService stadiumService;
    private final StadiumSearchService stadiumSearchService;

    private final Double DEFAULT_LAT = 37.5030;
    private final Double DEFAULT_LNT = 127.0416;

    @ApiOperation(value = "체육관 조회", notes = "사용자(일반)가 체육관을 조회한다.")
    @GetMapping()
    public ResponseEntity<?> getAllStadiums(Pageable pageable) {
        Page<StadiumResponseDto> stadiums = stadiumService.getAllStadiums(pageable);
        return ResponseEntity.ok().body(stadiums);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @ApiOperation(value = "체육관 상세 정보 조회", notes = "사용자(일반)가 체육관 상세 정보를 조회한다.")
    @GetMapping("/{stadiumId}/info")
    public ResponseEntity<?> getStadiumInfo(
            @PathVariable Long stadiumId,
            Pageable pageable
    ) {
        StadiumInfoResponseDto stadium = stadiumService.getStadiumInfo(stadiumId, pageable);
        return ResponseEntity.ok().body(stadium);
    }

    @ApiOperation(value = "가까운 체육관 조회", notes = "사용자(일반)의 위도 경도를 기준으로 가까운 체육관을 조회한다.")
    @GetMapping("/near-loc")
    public ResponseEntity<?> getAllStadiumsNearLocation(
            @RequestParam Double lnt, @RequestParam Double lat, Pageable pageable) {
        lnt = lnt == null ? DEFAULT_LNT : lnt;
        lat = lat == null ? DEFAULT_LAT : lat;
        List<StadiumResponseDto> stadiums = stadiumService.getAllStadiumsNearLocation(lnt, lat, pageable);
        return ResponseEntity.ok().body(stadiums);
    }

    @ApiOperation(value = "체육관 검색", notes = "검색어를 입력하여 체육관을 조회한다.")
    @PostMapping("/search")
    public ResponseEntity<?> searchStadium(
            @RequestBody SearchStadiumDto.Request request,
            Pageable pageable) {
        Page<StadiumDocument> stadiumDocuments = stadiumSearchService.search(request, pageable);
        return ResponseEntity.ok().body(stadiumDocuments);
    }
}