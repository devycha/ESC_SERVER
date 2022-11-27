package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.stadium.dto.CreateStadiumDto;
import com.minwonhaeso.esc.stadium.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stadiums")
public class StadiumController {
    private final StadiumService stadiumService;

    @PostMapping("/register")
    public ResponseEntity<?> createStadium(
            @RequestBody CreateStadiumDto.Request request) {
        CreateStadiumDto.Response stadium = stadiumService.createStadium(request);
        return ResponseEntity.ok().body(stadium);
    }

    @DeleteMapping("/{stadiumId}")
    public ResponseEntity<?> deleteStadium(@PathVariable Long stadiumId) {
        stadiumService.deleteStadium(stadiumId);
        return ResponseEntity.ok().build();
    }


}
