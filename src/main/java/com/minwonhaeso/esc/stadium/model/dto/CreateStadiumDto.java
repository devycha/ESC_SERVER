package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import lombok.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class CreateStadiumDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String name;
        private String phone;
        private String address;
        private Double lat;
        private Double lnt;
        private Integer weekdayPricePerHalfHour;
        private Integer holidayPricePerHalfHour;
        private Time openTime;
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