package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class CreateStadiumDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "체육관 생성 Request Body")
    public static class Request {
        private String name;
        private String phone;
        private String address;
        private Double lat;
        private Double lnt;
        private Integer weekdayPricePerHalfHour;
        private Integer holidayPricePerHalfHour;

        @ApiModelProperty(value = "오픈 시간", example = "HH:MM:SS")
        private Time openTime;

        @ApiModelProperty(value = "마감 시간", example = "HH:MM:SS")
        private Time closeTime;

        @Builder.Default
        private List<String> imgs = new ArrayList<>();

        @Builder.Default
        private List<String> tags = new ArrayList<>();

        @Builder.Default
        private List<CreateStadiumItemDto.Request> items = new ArrayList<>();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "체육관 생성 성공 Response Body")
    public static class Response {
//        private List<StadiumResponseDto> stadiums;
//
//        public List<StadiumResponseDto> fromEntities(Collection<Stadium> stadiumList) {
//            return stadiumList.stream().map(StadiumResponseDto::fromEntity)
//                    .collect(Collectors.toList());
//        }

        private StadiumResponseDto stadium;

        public static Response fromEntity(Stadium stadium) {
            return Response.builder()
                    .stadium(StadiumResponseDto.fromEntity(stadium))
                    .build();
        }
    }
}