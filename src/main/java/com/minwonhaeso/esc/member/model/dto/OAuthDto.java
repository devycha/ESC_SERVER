package com.minwonhaeso.esc.member.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.*;

public class OAuthDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(value = "소셜 로그인 Request Body")
    public static class Request {
        private String email;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(value = "소셜 로그인 Response Body")
    public static class Response {
        private String nickName;
        private String imgUrl;
        private String refreshToken;
    }
}
