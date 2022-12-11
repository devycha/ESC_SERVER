package com.minwonhaeso.esc.member.model.dto;

import lombok.*;

public class CPasswordDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request{
        private String email;
        private String prePassword;
        private String newPassword;
        private String confirmPassword;
        private Boolean hasToken;
    }
}
