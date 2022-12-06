package com.minwonhaeso.esc.stadium.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.*;

import java.sql.Time;

public class UpdateStadiumDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "체육관 정보 수정 Request Body")
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
    @ApiModel(value = "체육관 이미지 추가 Request Body")
    public static class AddImgRequest {
        private String imgUrl;
    }

    @Data
    @ApiModel(value = "체육관 이미지 삭제 Request Body")
    public static class DeleteImgRequest {
        private String imgUrl;
    }

    @Data
    @ApiModel(value = "체육관 종목(태그) 추가 Request Body")
    public static class AddTagRequest {
        private String tagName;
    }

    @Data
    @ApiModel(value = "체육관 종목(태그) 삭제 Request Body")
    public static class DeleteTagRequest {
        private String tagName;
    }

    @Data
    @ApiModel(value = "체육관 아이템 삭제 Request Body")
    public static class DeleteItemRequest {
        private Long itemId;
    }
}
