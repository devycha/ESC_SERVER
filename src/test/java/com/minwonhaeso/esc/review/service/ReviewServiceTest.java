package com.minwonhaeso.esc.review.service;

import com.minwonhaeso.esc.error.exception.ReviewException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.error.type.ReviewErrorCode;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.notification.repository.NotificationRepository;
import com.minwonhaeso.esc.notification.service.NotificationService;
import com.minwonhaeso.esc.review.model.dto.ReviewDto;
import com.minwonhaeso.esc.review.model.entity.Review;
import com.minwonhaeso.esc.review.repository.ReviewRepository;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import com.minwonhaeso.esc.stadium.repository.StadiumRepository;
import com.minwonhaeso.esc.stadium.repository.StadiumReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private StadiumReservationRepository reservationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("리뷰 작성 실패 - 존재하지 않는 체육관")
    void createReviewFail_StadiumNotFound() {
        Long stadiumId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        StadiumException exception = assertThrows(
                StadiumException.class,
                () -> reviewService.createReview(request, member, stadiumId));

        // then
        assertEquals(exception.getErrorCode(), StadiumErrorCode.StadiumNotFound);
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 존재하지 않는 체육관 사용 내역")
    void createReviewFail_NoReservationForReview() {
        Long stadiumId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .build();

        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));

        given(reservationRepository.countAllByMemberAndStadiumAndStatusIs(any(), any(), any()))
                .willReturn(0L);

        // when
        ReviewException exception = assertThrows(
                ReviewException.class,
                () -> reviewService.createReview(request, member, stadiumId));

        // then
        assertEquals(exception.getErrorCode(), ReviewErrorCode.NoReservationForReview);
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 리뷰 작성 가능 횟수 초과")
    void createReviewFail_ReviewCountOverReservation() {
        Long stadiumId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .build();

        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));

        given(reservationRepository.countAllByMemberAndStadiumAndStatusIs(any(), any(), any()))
                .willReturn(2L);

        given(reviewRepository.countAllByMemberAndStadium(any(), any()))
                .willReturn(2L);

        // when
        ReviewException exception = assertThrows(
                ReviewException.class,
                () -> reviewService.createReview(request, member, stadiumId));

        // then
        assertEquals(exception.getErrorCode(), ReviewErrorCode.ReviewCountOverReservation);
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReviewSuccess() {
        Long stadiumId = 1L;
        Long reviewId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .build();

        Review review = Review.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .member(member)
                .build();

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));

        given(reviewRepository.findByIdAndStadium(anyLong(), any()))
                .willReturn(Optional.of(review));

        // when
        reviewService.deleteReview(member, stadiumId, reviewId);
        Optional<Review> result = reviewRepository.findById(reviewId);

        // then
        verify(reviewRepository, times(1)).delete(captor.capture());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 체육관")
    void deleteReviewFail_StadiumNotFound() {
        Long stadiumId = 1L;
        Long reviewId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        StadiumException exception = assertThrows(
                StadiumException.class,
                () -> reviewService.deleteReview(member, stadiumId, reviewId));

        // then
        assertEquals(exception.getErrorCode(), StadiumErrorCode.StadiumNotFound);
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰")
    void deleteReviewFail_ReviewNotFound() {
        Long stadiumId = 1L;
        Long reviewId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));

        given(reviewRepository.findByIdAndStadium(anyLong(), any()))
                .willReturn(Optional.empty());

        // when
        ReviewException exception = assertThrows(
                ReviewException.class,
                () -> reviewService.deleteReview(member, stadiumId, reviewId));

        // then
        assertEquals(exception.getErrorCode(), ReviewErrorCode.ReviewNotFound);
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 리뷰 작성자 미일치")
    void deleteReviewFail_UnAuthorizedAccess() {
        Long stadiumId = 1L;
        Long reviewId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        Member member1 = Member.builder()
                .memberId(2L)
                .name("제로1")
                .password("1111")
                .email("test1@gmail.com")
                .build();

        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .build();

        Review review = Review.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .member(member1)
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));

        given(reviewRepository.findByIdAndStadium(anyLong(), any()))
                .willReturn(Optional.of(review));

        // when
        ReviewException exception = assertThrows(
                ReviewException.class,
                () -> reviewService.deleteReview(member, stadiumId, reviewId));

        // then
        assertEquals(exception.getErrorCode(), ReviewErrorCode.UnAuthorizedAccess);
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReviewSuccess() {
        Long stadiumId = 1L;
        Long reviewId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .build();

        Review review = Review.builder()
                .id(1L)
                .star(4.0)
                .comment("쾌적합니다")
                .createdAt(LocalDateTime.now())
                .member(member)
                .build();

        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(3.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .build();

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));

        given(reviewRepository.findByIdAndStadium(anyLong(), any()))
                .willReturn(Optional.of(review));

        // when
        ReviewDto.Response response= reviewService.updateReview(request, member, stadiumId, reviewId);

        // then
        verify(reviewRepository, times(1)).save(captor.capture());
        assertEquals(response.getStar(), 3.0);
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 체육관")
    void updateReviewFail_StadiumNotFound() {
        Long stadiumId = 1L;
        Long reviewId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(3.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        StadiumException exception = assertThrows(
                StadiumException.class,
                () -> reviewService.updateReview(request, member, stadiumId, reviewId));

        // then
        assertEquals(exception.getErrorCode(), StadiumErrorCode.StadiumNotFound);
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
    void updateReviewFail_ReviewNotFound() {
        Long stadiumId = 1L;
        Long reviewId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .build();

        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(3.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));

        given(reviewRepository.findByIdAndStadium(anyLong(), any()))
                .willReturn(Optional.empty());

        // when
        ReviewException exception = assertThrows(
                ReviewException.class,
                () -> reviewService.updateReview(request, member, stadiumId, reviewId));

        // then
        assertEquals(exception.getErrorCode(), ReviewErrorCode.ReviewNotFound);
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰 작성자 미일치")
    void updateReviewFail_UnAuthorizedAccess() {
        Long stadiumId = 1L;
        Long reviewId = 1L;

        Member member = Member.builder()
                .memberId(1L)
                .name("제로")
                .password("1111")
                .email("test@gmail.com")
                .build();

        Member member1 = Member.builder()
                .memberId(2L)
                .name("제로1")
                .password("1111")
                .email("test1@gmail.com")
                .build();

        Stadium stadium = Stadium.builder()
                .id(1L)
                .name("ESC 체육관")
                .phone("010-1234-5678")
                .address("경기도 광교")
                .detailAddress("123-456")
                .lat(36.5)
                .lnt(127.5)
                .weekdayPricePerHalfHour(19000)
                .holidayPricePerHalfHour(25000)
                .openTime(ReservingTime.findTime("09:00"))
                .closeTime(ReservingTime.findTime("18:00"))
                .build();

        Review review = Review.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .member(member1)
                .build();

        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(3.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .build();

        // given
        given(stadiumRepository.findById(anyLong()))
                .willReturn(Optional.of(stadium));

        given(reviewRepository.findByIdAndStadium(anyLong(), any()))
                .willReturn(Optional.of(review));

        // when
        ReviewException exception = assertThrows(
                ReviewException.class,
                () -> reviewService.updateReview(request, member, stadiumId, reviewId));

        // then
        assertEquals(exception.getErrorCode(), ReviewErrorCode.UnAuthorizedAccess);
    }
}