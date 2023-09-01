package com.minwonhaeso.esc.member.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PatchInfo {
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        private String nickname;
        private String imgUrl;
    }

}
