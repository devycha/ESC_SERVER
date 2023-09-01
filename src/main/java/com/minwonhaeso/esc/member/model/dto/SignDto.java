package com.minwonhaeso.esc.member.model.dto;

import com.minwonhaeso.esc.member.model.type.MemberType;
import lombok.*;


public class SignDto {
    @Builder
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String type;
        private String email;
        private String name;
        private String password;
        private String nickname;
        private String image;
        private String key;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String name;
        private String nickname;
        private String image;
    }
}
