package com.minwonhaeso.esc.member.model.dto;

import com.sun.istack.NotNull;
import lombok.*;


public class LoginDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        private String email;
        @NotNull
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class Response {
        private String accessToken;
        private String refreshToken;
        private Long id;
        private String name;
        private String nickname;
        private String imgUrl;

        public static Response of(Long id, String username, String nickname, String imgUrl, String accessToken, String refreshToken) {
            return Response.builder()
                    .id(id)
                    .name(username)
                    .nickname(nickname)
                    .imgUrl(imgUrl)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
    }
}
