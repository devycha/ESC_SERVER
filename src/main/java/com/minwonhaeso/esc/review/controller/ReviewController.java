package com.minwonhaeso.esc.review.controller;

import com.minwonhaeso.esc.review.model.dto.ReviewDto;
import com.minwonhaeso.esc.review.service.ReviewService;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/stadiums")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{stadiumId}/reviews")
    public ResponseEntity<?> getAllReviews(
            @PathVariable Long stadiumId,
            Pageable pageable
    ) {
        Page<ReviewDto.Response> reviews = reviewService.getAllReviewsByStadium(stadiumId, pageable);
        return ResponseEntity.ok().body(reviews);
    }

    @PostMapping("/{stadiumId}/reviews")
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody ReviewDto.Request request
    ) {
        // TODO 리뷰 등록
        return null;
    }

    @DeleteMapping("/{stadiumId}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable(value = "stadiumId") Long stadiumId,
            @PathVariable("reviewId") Long reviewId,
            @RequestBody ReviewDto.Request request
    ) {
        // TODO 리뷰 삭제 (+ PathVariable 방식 수정)
        return null;
    }

    @PatchMapping("/{stadiumId}/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable(value = "stadiumId") Long stadiumId,
            @PathVariable("reviewId") Long reviewId,
            @RequestBody ReviewDto.Request request
    ) {
        // TODO 리뷰 수정 (+ PathVariable 방식 수정)
        return null;
    }
}
