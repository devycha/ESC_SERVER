package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumDocument;
import com.minwonhaeso.esc.stadium.model.entity.StadiumTag;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "체육관 정보 Response")
public class StadiumResponseDto {
    @ApiModelProperty(value = "체육관 ID", example = "1")
    private Long stadiumId;

    @ApiModelProperty(value = "체육관 이름", example = "수원 국립 체육관")
    private String name;

    @ApiModelProperty(value = "위도", example = "37.5")
    private Double lat;

    @ApiModelProperty(value = "경도", example = "127")
    private Double lnt;

    @ApiModelProperty(value = "주소", example = "경기도 수원시")
    private String address;

    @ApiModelProperty(value = "평점", example = "4.5")
    private Double starAvg;

    @ApiModelProperty(value = "평일 가격", example = "20000")
    private Integer weekdayPricePerHalfHour;

    @ApiModelProperty(value = "휴일 가격", example = "30000")
    private Integer holidayPricePerHalfHour;

    @ApiModelProperty(value = "이미지", example = "['img_url1', 'img_url2', ...]")
    private String imgUrl; // TODO: 이미지주소 + public_id도 포함 필요
    private List<String> tags;


    public static StadiumResponseDto fromEntity(Stadium stadium) {
        return StadiumResponseDto.builder()
                .stadiumId(stadium.getId())
                .name(stadium.getName())
                .lat(stadium.getLat())
                .lnt(stadium.getLnt())
                .address(stadium.getAddress())
                .starAvg(stadium.getStarAvg())
                .weekdayPricePerHalfHour(stadium.getWeekdayPricePerHalfHour())
                .holidayPricePerHalfHour(stadium.getHolidayPricePerHalfHour())
                .imgUrl(stadium.getImgs().isEmpty() ?
                        null :
                        stadium.getImgs().get(0).getImgUrl())
                .tags(stadium.getTags().stream().map(StadiumTag::getName).collect(Collectors.toList()))
                .build();
    }

    public static StadiumResponseDto fromDocument(StadiumDocument document) {
        return StadiumResponseDto.builder()
                .stadiumId(document.getId())
                .name(document.getName())
                .lat(document.getLat())
                .lnt(document.getLnt())
                .address(document.getAddress())
                .starAvg(document.getStarAvg())
                .weekdayPricePerHalfHour(document.getWeekdayPricePerHalfHour())
                .holidayPricePerHalfHour(document.getHolidayPricePerHalfHour())
                .imgUrl(document.getImgUrl())
                .tags(document.getTags())
                .build();

    }
}
