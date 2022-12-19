package com.minwonhaeso.esc.review.service;

import com.minwonhaeso.esc.error.exception.ReviewException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.notification.service.NotificationService;
import com.minwonhaeso.esc.review.model.dto.ReviewDto;
import com.minwonhaeso.esc.review.model.entity.Review;
import com.minwonhaeso.esc.review.repository.ReviewRepository;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.type.StadiumReservationStatus;
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
import static com.minwonhaeso.esc.notification.model.type.NotificationType.REVIEW;

@RequiredArgsConstructor
@Slf4j
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final StadiumRepository stadiumRepository;
    private final StadiumReservationRepository stadiumReservationRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<ReviewDto.Response> getAllReviewsByStadium(Long stadiumId, Pageable pageable) {
        Stadium stadium = findByStadiumId(stadiumId);
        return reviewRepository.findAllByStadium(stadium, pageable).map(ReviewDto.Response::fromEntity);
    }

    @Transactional
    public ReviewDto.Response createReview(ReviewDto.Request request, Member member, Long stadiumId) {
        Stadium stadium = findByStadiumId(stadiumId);

        Long reservationCount = stadiumReservationRepository.countAllByMemberAndStadiumAndStatusIs(
                member, stadium, StadiumReservationStatus.EXECUTED);
        log.info("[Stadium - " + stadiumId + "] 사용 횟수는 총 " + reservationCount + "번입니다.");

        Long reviewCount = reviewRepository.countAllByMemberAndStadium(member, stadium);
        log.info("[Stadium - " + stadiumId + "] 사용 후, 작성하신 리뷰는 총 " + reviewCount + "번입니다.");

        if(reservationCount == 0) {
            throw new ReviewException(NoReservationForReview);
        } else if (reservationCount <= reviewCount) {
            throw new ReviewException(ReviewCountOverReservation);
        }

        Review review = Review.builder()
                .star(request.getStar())
                .comment(request.getComment())
                .stadium(stadium)
                .member(member)
                .build();

        reviewRepository.save(review);
        log.info("회원 번호 [ " + member.getMemberId() + " ] -  리뷰를 작성하였습니다.");

        notificationService.createNotification(REVIEW, stadiumId, 0L,
                "체육관 [ " + stadium.getName() + " ]에 새로운 리뷰가 등록되었습니다.", stadium.getMember());
        log.info("회원 번호 [ " + stadium.getMember().getMemberId() + " ] 로 알람이 발송되었습니다.");

        return ReviewDto.Response.fromEntity(review);
    }

    public void deleteReview(Member member, Long stadiumId, Long reviewId) {
        Stadium stadium = findByStadiumId(stadiumId);
        Review review = findByIdAndStadiumAndMember(reviewId, stadium, member);

        if (review.getMember().getMemberId() != member.getMemberId()) {
            throw new ReviewException(UnAuthorizedAccess);
        }

        reviewRepository.delete(review);
        log.info("회원 번호 [ " + member.getMemberId() + " ] - [ " + reviewId + " ] 리뷰를 삭제하였습니다.");
    }

    public ReviewDto.Response updateReview(ReviewDto.Request request, Member member, Long stadiumId, Long reviewId) {
        Stadium stadium = findByStadiumId(stadiumId);
        Review review = findByIdAndStadiumAndMember(reviewId, stadium, member);

        if (review.getMember().getMemberId() != member.getMemberId()) {
            throw new ReviewException(UnAuthorizedAccess);
        }

        review.update(request.getStar(), request.getComment());
        reviewRepository.save(review);
        log.info("회원 번호 [ " + member.getMemberId() + " ] - [ " + reviewId + " ] 리뷰를 수정하였습니다.");

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