package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.stadium.model.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stadiums")
public class StadiumUserController {
    private final StadiumService stadiumService;
    private final Double DEFAULT_LAT = 37.5030;
    private final Double DEFAULT_LNT = 127.0416;

    @GetMapping()
    public ResponseEntity<?> getAllStadiums(Pageable pageable) {
        Page<StadiumResponseDto> stadiums = stadiumService.getAllStadiums(pageable);
        return ResponseEntity.ok().body(stadiums);
    }

    @GetMapping("/near-loc")
    public ResponseEntity<?> getAllStadiumsNearLocation(
            @RequestParam Map<String, String> params, Pageable pageable) {
        Double lnt = Double.valueOf(params.getOrDefault("lnt", String.valueOf(DEFAULT_LNT)));
        Double lat = Double.valueOf(params.getOrDefault("lat", String.valueOf(DEFAULT_LAT)));

        List<StadiumResponseDto> stadiums = stadiumService.getAllStadiumsNearLocation(lnt, lat, pageable);
        return ResponseEntity.ok().body(stadiums);
    }
}