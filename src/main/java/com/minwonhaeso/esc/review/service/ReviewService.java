package com.minwonhaeso.esc.review.service;

import com.minwonhaeso.esc.error.exception.ReviewException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.review.model.dto.ReviewDto;
import com.minwonhaeso.esc.review.model.entity.Review;
import com.minwonhaeso.esc.review.repository.ReviewRepository;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.minwonhaeso.esc.error.type.ReviewErrorCode.*;
import static com.minwonhaeso.esc.error.type.StadiumErrorCode.StadiumNotFound;

@RequiredArgsConstructor
@Slf4j
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final StadiumRepository stadiumRepository;
    private final StadiumReservationRepository stadiumReservationRepository;

    @Transactional(readOnly = true)
    public Page<ReviewDto.Response> getAllReviewsByStadium(Long stadiumId, Pageable pageable) {
        Stadium stadium = findByStadiumId(stadiumId);
        return reviewRepository.findAllByStadium(stadium, pageable).map(ReviewDto.Response::fromEntity);
    }

    @Transactional
    public ReviewDto.Response createReview(ReviewDto.Request request, Member member, Long stadiumId) {
        Stadium stadium = findByStadiumId(stadiumId);

        long reservationCount = stadiumReservationRepository.countAllByMember(member);
        long reviewCount = reviewRepository.countAllByMember(member);

//        if(reservationCount == 0) {
//            throw new ReviewException(NoReservationForReview);
//        } else if (reservationCount <= reviewCount) {
//            throw new ReviewException(ReviewCountOverReservation);
//        }

        Review review = Review.builder()
                .star(request.getStar())
                .comment(request.getComment())
                .stadium(stadium)
                .member(member)
                .build();

        reviewRepository.save(review);
        log.info("회원 번호 [ " + member.getMemberId() + " ] -  리뷰를 작성하였습니다.");

        return ReviewDto.Response.fromEntity(review);
    }

    public void deleteReview(Member member, Long stadiumId, Long reviewId) {
        Stadium stadium = findByStadiumId(stadiumId);
        Review review = findByIdAndStadiumAndMember(reviewId, stadium, member);

        if (review.getMember().getMemberId() != member.getMemberId()) {
            throw new ReviewException(UnAuthorizedAccess);
        }

        reviewRepository.delete(review);
        log.info("회원 번호 [ " + member.getMemberId() + " ] -  리뷰를 삭제하였습니다.");
    }

    public ReviewDto.Response updateReview(ReviewDto.Request request, Member member, Long stadiumId, Long reviewId) {
        Stadium stadium = findByStadiumId(stadiumId);
        Review review = findByIdAndStadiumAndMember(reviewId, stadium, member);

        if (review.getMember().getMemberId() != member.getMemberId()) {
            throw new ReviewException(UnAuthorizedAccess);
        }

        review.update(request.getStar(), request.getComment());
        reviewRepository.save(review);
        log.info("회원 번호 [ " + member.getMemberId() + " ] -  리뷰를 수정하였습니다.");

        return ReviewDto.Response.fromEntity(review);
    }

    private Stadium findByStadiumId(Long stadiumId) {
        return stadiumRepository.findById(stadiumId).orElseThrow(() -> new StadiumException(StadiumNotFound));
    }

    private Review findByIdAndStadiumAndMember(Long reviewId, Stadium stadium, Member member) {
        return reviewRepository.findByIdAndStadiumAndMember(reviewId, stadium, member)
                .orElseThrow(() -> new ReviewException(ReviewNotFound));
    }

}