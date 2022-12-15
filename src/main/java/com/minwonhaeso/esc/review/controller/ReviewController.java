package com.minwonhaeso.esc.review.controller;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.review.model.dto.ReviewDto;
import com.minwonhaeso.esc.review.service.ReviewService;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{stadiumId}/reviews")
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @RequestBody ReviewDto.Request request
    ) {
        Member member = principalDetail.getMember();
        ReviewDto.Response review = reviewService.createReview(request, member, stadiumId);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{stadiumId}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @PathVariable Long reviewId
    ) {
        Member member = principalDetail.getMember();
        reviewService.deleteReview(member, stadiumId, reviewId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PatchMapping("/{stadiumId}/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable Long stadiumId,
            @PathVariable Long reviewId,
            @RequestBody ReviewDto.Request request
    ) {
        Member member = principalDetail.getMember();
        ReviewDto.Response review = reviewService.updateReview(request, member, stadiumId, reviewId);
        return ResponseEntity.ok().body(review);
    }
}
