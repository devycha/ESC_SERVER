package com.minwonhaeso.esc.review.model.dto;

import com.minwonhaeso.esc.review.model.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReviewDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private long star;
        private String comment;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private long star;
        private String comment;

        public static ReviewDto.Response fromEntity(Review review) {
            return Response.builder()
                    .star(review.getStar())
                    .comment(review.getComment())
                    .build();
        }
    }
}
