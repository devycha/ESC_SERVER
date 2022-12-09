package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class StadiumDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "체육관 생성 Request Body")
    public static class CreateStadiumRequest {
        private String name;
        private String phone;
        private String address;
        private String detailAddress;
        private Double lat;
        private Double lnt;
        private Integer weekdayPricePerHalfHour;
        private Integer holidayPricePerHalfHour;

        @ApiModelProperty(value = "오픈 시간", example = "HH:MM:SS")
        private Time openTime;

        @ApiModelProperty(value = "마감 시간", example = "HH:MM:SS")
        private Time closeTime;

        @Builder.Default
        private List<StadiumImgDto.CreateImgRequest> imgs = new ArrayList<>();

        @Builder.Default
        private List<String> tags = new ArrayList<>();

        @Builder.Default
        private List<StadiumItemDto.CreateItemRequest> items = new ArrayList<>();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "체육관 생성 성공 Response Body")
    public static class CreateStadiumResponse {
        private StadiumResponseDto stadium;

        public static CreateStadiumResponse fromEntity(Stadium stadium) {
            return CreateStadiumResponse.builder()
                    .stadium(StadiumResponseDto.fromEntity(stadium))
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "체육관 정보 수정 Request Body")
    public static class UpdateStadiumRequest {
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
}
