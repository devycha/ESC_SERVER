package com.minwonhaeso.esc.stadium.dto;

import com.minwonhaeso.esc.stadium.entity.Stadium;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.util.List;

public class CreateStadiumDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private List<String> imgs;
        private String name;
        private String phone;
        private String address;
        private Double lat;
        private Double lnt;
        private Integer weekdayPricePerHalfHour;
        private Integer holidayPricePerHalfHour;
        private List<String> tags;
        private Time openTime;
        private Time closeTime;
//        private List<Item> items; // TODO: 아이템 추가
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