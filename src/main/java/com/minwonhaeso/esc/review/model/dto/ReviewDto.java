package com.minwonhaeso.esc.review.model.dto;

import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.review.model.entity.Review;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        private Double star;
        private String comment;
        private MemberResponse member;
        private String createdAt;

        public static ReviewDto.Response fromEntity(Review review) {
            return Response.builder()
                    .id(review.getId())
                    .star(review.getStar())
                    .comment(review.getComment())
                    .member(MemberResponse.fromEntity(review.getMember()))
                    .createdAt(review.getCreatedAt().format(DateTimeFormatter
                            .ofPattern("yyyy-MM-dd a HH:mm").withLocale(Locale.forLanguageTag("ko"))))
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberResponse {
        private Long id;
        private String email;
        private String nickname;
        private String name;
        private String imgUrl;

        public static ReviewDto.MemberResponse fromEntity(Member member) {
            return MemberResponse.builder()
                    .id(member.getMemberId())
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .name(member.getName())
                    .imgUrl(member.getImgUrl())
                    .build();
        }
    }
}
