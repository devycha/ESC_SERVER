package com.minwonhaeso.esc.stadium.controller;

import com.minwonhaeso.esc.stadium.dto.CreateStadiumDto;
import com.minwonhaeso.esc.stadium.dto.CreateStadiumItemDto;
import com.minwonhaeso.esc.stadium.dto.StadiumResponseDto;
import com.minwonhaeso.esc.stadium.dto.UpdateStadiumDto;
import com.minwonhaeso.esc.stadium.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stadiums")
public class StadiumController {
    private final StadiumService stadiumService;

    @GetMapping()
    public ResponseEntity<?> getAllStadiums(Pageable pageable) {
        Page<StadiumResponseDto> stadiums = stadiumService.getAllStadiums(pageable);
        return ResponseEntity.ok().body(stadiums);
    }

    @GetMapping("/manager")
    public ResponseEntity<?> getAllRegisteredStadiumsByManager(@PathVariable String memberId) {
        // TODO: Member 도메인 작업 후 진행
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createStadiumByManager(
            @RequestBody CreateStadiumDto.Request request
    ) {
        CreateStadiumDto.Response stadium = stadiumService.createStadium(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(stadium);
    }

    @PatchMapping("/{stadiumId}/info")
    public ResponseEntity<?> updateStadiumInfo(
            @PathVariable Long stadiumId,
            UpdateStadiumDto.Request request
    ) {
        StadiumResponseDto stadium = stadiumService.updateStadiumInfo(stadiumId, request);
        return ResponseEntity.ok().body(stadium);
    }

    @PostMapping("/{stadiumId}/imgs")
    public ResponseEntity<?> addStadiumImgByManager(
            @PathVariable Long stadiumId,
            @RequestBody UpdateStadiumDto.AddImgRequest request
    ) {
        stadiumService.addStadiumImg(stadiumId, request.getImgUrl());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{stadiumId}/tags")
    public ResponseEntity<?> addStadiumTagByManager(
            @PathVariable Long stadiumId,
            @RequestBody UpdateStadiumDto.AddTagRequest request
    ) {
        stadiumService.addStadiumTag(stadiumId, request.getTagName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{stadiumId}/items")
    public ResponseEntity<?> addStadiumItemByManager(
            @PathVariable Long stadiumId,
            @RequestBody CreateStadiumItemDto.Request request
    )  {
        CreateStadiumItemDto.Response item = stadiumService.addStadiumItem(stadiumId, request);
        return ResponseEntity.ok().body(item);
    }

    @DeleteMapping("/{stadiumId}/imgs")
    public ResponseEntity<?> deleteStadiumImgByManager(
            @PathVariable Long stadiumId,
            @RequestBody UpdateStadiumDto.DeleteImgRequest request
    ) {
        stadiumService.deleteStadiumImg(stadiumId, request.getImgUrl());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{stadiumId}/tags")
    public ResponseEntity<?> deleteStadiumTagByManager(
            @PathVariable Long stadiumId,
            @RequestBody UpdateStadiumDto.DeleteTagRequest request
    ) {
        stadiumService.deleteStadiumTag(stadiumId, request.getTagName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{stadiumId}/items")
    public ResponseEntity<?> deleteStadiumItemByManager(
            @PathVariable Long stadiumId,
            @RequestBody UpdateStadiumDto.DeleteItemRequest request) {
        stadiumService.deleteStadiumItem(stadiumId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{stadiumId}")
    public ResponseEntity<?> deleteStadiumByManager(@PathVariable Long stadiumId) {
        stadiumService.deleteStadium(stadiumId);
        return ResponseEntity.ok().build();
    }
}