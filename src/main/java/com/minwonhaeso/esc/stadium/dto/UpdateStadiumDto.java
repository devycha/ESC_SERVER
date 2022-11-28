package com.minwonhaeso.esc.stadium.dto;

import lombok.*;

import java.sql.Time;

public class UpdateStadiumDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String name;
        private String phone;
        private Double lat;
        private Double lnt;
        private String address;
        private Integer weekdayPricePerHalfHour;
        private Integer holidayPricePerHalfHour;
        private Time openTime;
        private Time closeTime;
    }

    @Data
    public static class AddImgRequest {
        private String imgUrl;
    }

    @Data
    public static class DeleteImgRequest {
        private String imgUrl;
    }
}
