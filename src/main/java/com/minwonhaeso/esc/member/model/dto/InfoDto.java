package com.minwonhaeso.esc.member.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class InfoDto {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class Response{
        private String nickname;
        private String email;
        private String imgUrl;
    }
}
