package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

public class StadiumDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "체육관 생성 Request Body")
    public static class CreateStadiumRequest {
        @ApiModelProperty(required = true)
        private String name;
        @ApiModelProperty(required = true)
        private String phone;
        @ApiModelProperty(required = true)
        private String address;
        @ApiModelProperty(required = true)
        private String detailAddress;
        @ApiModelProperty(required = true)
        private Double lat;
        @ApiModelProperty(required = true)
        private Double lnt;
        @ApiModelProperty(required = true)
        private Integer weekdayPricePerHalfHour;
        @ApiModelProperty(required = true)
        private Integer holidayPricePerHalfHour;
        @ApiModelProperty(value = "오픈 시간", example = "HH:MM", required = true)
        private String openTime;
        @ApiModelProperty(value = "마감 시간", example = "HH:MM", required = true)
        private String closeTime;

        @Builder.Default
        private List<StadiumImgDto.CreateImgRequest> imgs = new ArrayList<>();

        @Builder.Default
        private List<String> tags = new ArrayList<>();

        @Builder.Default
        private List<StadiumItemDto.CreateItemRequest> rentalItems = new ArrayList<>();
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
    @ToString
    public static class UpdateStadiumRequest {
        private String name;
        private String phone;
        private Double lat;
        private Double lnt;
        private String address;
        private Integer weekdayPricePerHalfHour;
        private Integer holidayPricePerHalfHour;
        private String openTime;
        private String closeTime;
    }
}
