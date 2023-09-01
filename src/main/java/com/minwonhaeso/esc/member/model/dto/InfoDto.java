package com.minwonhaeso.esc.member.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class InfoDto {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class Response{
        private Long id;
        private String nickname;
        private String name;
        private String email;
        private String imgUrl;
    }
}
