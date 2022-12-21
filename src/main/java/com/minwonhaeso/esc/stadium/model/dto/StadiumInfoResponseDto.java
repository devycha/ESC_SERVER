package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumTag;
import com.minwonhaeso.esc.stadium.model.type.ReservingTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "체육관 상세 정보 조회 Response")
public class StadiumInfoResponseDto {
    @ApiModelProperty(value = "체육관 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "현재 접속한 사용자 아이디")
    private Long memberId;

    @ApiModelProperty(value = "체육관 이름", example = "수원 국립 체육관")
    private String name;

    @ApiModelProperty(value = "위도", example = "37.5")
    private Double lat;

    @ApiModelProperty(value = "경도", example = "127")
    private Double lnt;

    @ApiModelProperty(value = "주소", example = "경기도 수원시")
    private String address;

    @ApiModelProperty(value = "상세 주소", example = "302호")
    private String detailAddress;

    @ApiModelProperty(value = "전화번호", example="01012345678")
    private String phone;

    @ApiModelProperty(value = "평점", example = "4.5")
    private Double starAvg;

    @ApiModelProperty(value = "평일 30분당 가격", example = "20000")
    private Integer weekdayPricePerHalfHour;

    @ApiModelProperty(value = "공휴일 30분당 가격", example = "30000")
    private Integer holidayPricePerHalfHour;

    private List<StadiumItemDto.Response> rentalItems;

    @ApiModelProperty(value = "이미지", example = "['img_url1', 'img_url2', ...]")
    private List<StadiumImgDto> imgs;
    private List<String> tags;

    @ApiModelProperty(value = "오픈 시간", example = "HH:MM:SS")
    private String openTime;

    @ApiModelProperty(value = "마감 시간", example = "HH:MM:SS")
    private String closeTime;

    public static StadiumInfoResponseDto fromEntity(Stadium stadium) {
        return StadiumInfoResponseDto.builder()
                .id(stadium.getId())
                .memberId(stadium.getMember().getMemberId())
                .name(stadium.getName())
                .lat(stadium.getLat())
                .lnt(stadium.getLnt())
                .phone(stadium.getPhone())
                .address(stadium.getAddress())
                .detailAddress(stadium.getDetailAddress())
                .starAvg(stadium.getStarAvg())
                .weekdayPricePerHalfHour(stadium.getWeekdayPricePerHalfHour())
                .holidayPricePerHalfHour(stadium.getHolidayPricePerHalfHour())
                .openTime(ReservingTime.valueOf(stadium.getOpenTime()).getTime())
                .closeTime(ReservingTime.valueOf(stadium.getCloseTime()).getTime())
                .rentalItems(stadium.getRentalStadiumItems().stream()
                        .map(StadiumItemDto.Response::fromEntity)
                        .collect(Collectors.toList()))
                .imgs(stadium.getImgs().isEmpty() ?
                        null :
                        stadium.getImgs().stream().map(img ->
                                StadiumImgDto.builder()
                                        .id(img.getId())
                                        .publicId(img.getImgId())
                                        .imgUrl(img.getImgUrl())
                                        .build())
                        .collect(Collectors.toList()))
                .tags(stadium.getTags().stream().map(StadiumTag::getName).collect(Collectors.toList()))
                .build();
    }
}
