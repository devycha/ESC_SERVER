package com.minwonhaeso.esc.review.model.dto;

import com.minwonhaeso.esc.review.model.entity.Review;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ReviewDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "리뷰 Request Body")
    public static class Request {
        private Double star;
        private String comment;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "리뷰 Response Body")
    public static class Response {
        private Long id;
        private Long memberId;
        private Double star;
        private String comment;
        private String nickname;
        private String imgUrl;
        private String createdAt;

        public static ReviewDto.Response fromEntity(Review review) {
            return Response.builder()
                    .id(review.getId())
                    .memberId(review.getMember().getMemberId())
                    .star(review.getStar())
                    .comment(review.getComment())
                    .nickname(review.getMember().getNickname())
                    .imgUrl(review.getMember().getImgUrl())
                    .createdAt(review.getCreatedAt().format(DateTimeFormatter
                            .ofPattern("yyyy-MM-dd a HH:mm").withLocale(Locale.forLanguageTag("ko"))))
                    .build();
        }
    }
}
