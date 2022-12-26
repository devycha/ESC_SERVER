package com.minwonhaeso.esc.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minwonhaeso.esc.error.exception.ReviewException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.model.type.MemberRole;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.review.model.dto.ReviewDto;
import com.minwonhaeso.esc.review.model.entity.Review;
import com.minwonhaeso.esc.review.service.ReviewService;
import com.minwonhaeso.esc.security.WebSecurityConfig;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.security.auth.jwt.JwtAuthenticationFilter;
import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static com.minwonhaeso.esc.error.type.ReviewErrorCode.*;
import static com.minwonhaeso.esc.error.type.StadiumErrorCode.StadiumNotFound;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ReviewController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                WebSecurityConfig.class,
                                JwtAuthenticationFilter.class
                        }
                )
        }
)
@AutoConfigureMockMvc
@MockBean(JpaMetamodelMappingContext.class)
class ReviewControllerTest {

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private Member otherMember;
    private Stadium stadium;
    private Review review;
    private PageRequest pageable;
    private Page<ReviewDto.Response> reviews;

    @BeforeEach
    void beforeEach() {
        member = Member.builder()
                .memberId(1L)
                .name("user")
                .nickname("Nickname")
                .password("1111")
                .email("test@gmail.com")
                .imgUrl("Image URL")
                .type(MemberType.USER)
                .role(MemberRole.ROLE_USER)
                .build();

        otherMember = Member.builder()
                .memberId(2L)
                .name("other")
                .nickname("other")
                .password("1111")
                .email("other@gmail.com")
                .imgUrl("Image URL")
                .type(MemberType.USER)
                .role(MemberRole.ROLE_USER)
                .build();

        stadium = Stadium.builder()
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

        review = Review.builder()
                .id(1L)
                .star(4.0)
                .comment("쾌적합니다")
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();

        pageable = PageRequest.of(0, 20);
        reviews = new PageImpl<>(List.of(ReviewDto.Response.fromEntity(review)), pageable, 0);
    }

    @Test
    @WithMockUser()
    @DisplayName("리뷰 조회 성공")
    void getAllReviewsSuccess() throws Exception {
        // given
        given(reviewService.getAllReviewsByStadium(any(), any()))
                .willReturn(reviews);

        // then
        mockMvc.perform(get("/stadiums/1/reviews"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(review.getId()))
                .andExpect(jsonPath("$.content[0].star").value(review.getStar()))
                .andExpect(jsonPath("$.content[0].comment").value(review.getComment()))
                .andExpect(jsonPath("$.content[0].createdAt").value(
                        review.getCreatedAt().format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd a HH:mm").withLocale(Locale.forLanguageTag("ko")))))
                .andExpect(jsonPath("$.content.size()").value(reviews.getTotalElements()));
    }

    @Test
    @WithMockUser(username="user")
    @DisplayName("리뷰 작성 성공")
    void createReviewSuccess() throws Exception {
        // given
        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .build();

        ReviewDto.Response response = ReviewDto.Response.builder()
                .id(1L)
                .star(4.0)
                .comment("쾌적합니다")
                .member(ReviewDto.MemberResponse.fromEntity(member))
                .createdAt(LocalDateTime.now().format(DateTimeFormatter
                        .ofPattern("yyyy-MM-dd a HH:mm").withLocale(Locale.forLanguageTag("ko"))))
                .build();

        // when
        Mockito.when(reviewService.createReview(any(), any(), anyLong())).thenReturn(response);
        String requestJson = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(post("/stadiums/1/reviews").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.star").value(response.getStar()))
                .andExpect(jsonPath("$.comment").value(response.getComment()))
                .andExpect(jsonPath("$.createdAt").value(
                        review.getCreatedAt().format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd a HH:mm").withLocale(Locale.forLanguageTag("ko")))));
    }

    @Test
    @WithMockUser(username="user")
    @DisplayName("리뷰 작성 실패 - 존재하지 않는 체육관")
    void createReviewFail_StadiumNotFound() throws Exception {
        // given
        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .build();

        // when
        Mockito.when(reviewService.createReview(any(), any(), anyLong()))
                .thenThrow(new StadiumException(StadiumNotFound));

        String requestJson = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(post("/stadiums/1/reviews").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(StadiumNotFound.getErrorMessage()));
    }

    @Test
    @WithMockUser(username="user")
    @DisplayName("리뷰 작성 실패 - 존재하지 않는 체육관 사용 내역")
    void createReviewFail_NoReservationForReview() throws Exception {
        // given
        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .build();

        // when
        Mockito.when(reviewService.createReview(any(), any(), anyLong()))
                .thenThrow(new ReviewException(NoExecutedReservationForReview));

        String requestJson = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(post("/stadiums/1/reviews").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(NoExecutedReservationForReview.getErrorMessage()));
    }

    @Test
    @WithMockUser(username="user")
    @DisplayName("리뷰 작성 실패 - 리뷰 작성 가능 횟수 초과")
    void createReviewFail_ReviewCountOverReservation() throws Exception {
        // given
        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(4.0)
                .comment("쾌적합니다")
                .build();

        // when
        Mockito.when(reviewService.createReview(any(), any(), anyLong()))
                .thenThrow(new ReviewException(ReviewCountOverReservation));

        String requestJson = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(post("/stadiums/1/reviews").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(ReviewCountOverReservation.getErrorMessage()));
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    @WithMockUser(username="user")
    void deleteReviewSuccess() throws Exception {
        // when
        doNothing().when(reviewService).deleteReview(any(), anyLong(), anyLong());

        // then
        mockMvc.perform(delete("/stadiums/1/reviews/1").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 체육관")
    @WithMockUser(username="user")
    void deleteReviewFail_StadiumNotFound() throws Exception {
        // when
        doThrow(new StadiumException(StadiumNotFound)).when(reviewService).deleteReview(any(), anyLong(), anyLong());

        // then
        mockMvc.perform(delete("/stadiums/5/reviews/1").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰")
    @WithMockUser(username="user")
    void deleteReviewFail_ReviewNotFound() throws Exception {
        // when
        doThrow(new ReviewException(ReviewNotFound)).when(reviewService).deleteReview(any(), anyLong(), anyLong());

        // then
        mockMvc.perform(delete("/stadiums/1/reviews/1").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 리뷰 작성자 미일치")
    @WithMockUser(username="other")
    void deleteReviewFail_UnAuthorizedAccess() throws Exception {
        // when
        doThrow(new ReviewException(UnAuthorizedAccess)).when(reviewService).deleteReview(any(), anyLong(), anyLong());

        // then
        mockMvc.perform(delete("/stadiums/1/reviews/1").with(user(PrincipalDetail.of(otherMember)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    @WithMockUser(username="user")
    void updateReviewSuccess() throws Exception {
        // given
        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(2.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .build();

        ReviewDto.Response response = ReviewDto.Response.builder()
                .id(1L)
                .star(2.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .member(ReviewDto.MemberResponse.fromEntity(member))
                .createdAt(review.getCreatedAt().format(DateTimeFormatter
                        .ofPattern("yyyy-MM-dd a HH:mm").withLocale(Locale.forLanguageTag("ko"))))
                .build();

        // when
        Mockito.when(reviewService.updateReview(any(), any(), anyLong(), anyLong())).thenReturn(response);
        String requestJson = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(patch("/stadiums/1/reviews/1").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.star").value(response.getStar()))
                .andExpect(jsonPath("$.comment").value(response.getComment()))
                .andExpect(jsonPath("$.createdAt").value(
                        review.getCreatedAt().format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd a HH:mm").withLocale(Locale.forLanguageTag("ko")))));
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 체육관")
    @WithMockUser(username="user")
    void updateReviewFail_StadiumNotFound() throws Exception {
        // given
        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(2.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .build();

        // when
        Mockito.when(reviewService.updateReview(any(), any(), anyLong(), anyLong()))
                .thenThrow(new StadiumException(StadiumNotFound));

        String requestJson = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(patch("/stadiums/5/reviews/1").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(StadiumNotFound.getErrorMessage()));
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
    @WithMockUser(username="user")
    void updateReviewFail_ReviewNotFound() throws Exception {
        // given
        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(2.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .build();

        // when
        Mockito.when(reviewService.updateReview(any(), any(), anyLong(), anyLong()))
                .thenThrow(new ReviewException(ReviewNotFound));

        String requestJson = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(patch("/stadiums/1/reviews/2").with(user(PrincipalDetail.of(member)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(ReviewNotFound.getErrorMessage()));
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰 작성자 미일치")
    @WithMockUser(username="other")
    void updateReviewFail_UnAuthorizedAccess() throws Exception {
        // given
        ReviewDto.Request request = ReviewDto.Request.builder()
                .star(2.0)
                .comment("시설이 제대로 관리되는 것 같지 않습니다")
                .build();

        // when
        Mockito.when(reviewService.updateReview(any(), any(), anyLong(), anyLong()))
                .thenThrow(new ReviewException(UnAuthorizedAccess));

        String requestJson = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(patch("/stadiums/1/reviews/2").with(user(PrincipalDetail.of(otherMember)))
                        .with(csrf())
                        .header("Authorization", "Bearer accessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").value(UnAuthorizedAccess.getErrorMessage()));
    }
}