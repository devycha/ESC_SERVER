package com.minwonhaeso.esc.stadium.model.dto;

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

    @Data
    public static class AddTagRequest {
        private String tagName;
    }

    @Data
    public static class DeleteTagRequest {
        private String tagName;
    }

    @Data
    public static class DeleteItemRequest {
        private Long itemId;
    }
}
