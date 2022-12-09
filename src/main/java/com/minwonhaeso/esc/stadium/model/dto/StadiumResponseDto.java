package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumImg;
import com.minwonhaeso.esc.stadium.model.entity.StadiumTag;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "체육관 상세 정보")
public class StadiumResponseDto {
    @ApiModelProperty(value = "체육관 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "위도", example = "37.5")
    private Double lat;

    @ApiModelProperty(value = "경도", example = "127")
    private Double lnt;

    @ApiModelProperty(value = "주소", example = "경기도 수원시")
    private String address;

    @ApiModelProperty(value = "평점", example = "4.5")
    private Double star_avg;

    @ApiModelProperty(value = "찜하기 수", example = "8")
    private Integer likes;

    @ApiModelProperty(value = "이미지", example = "['img_url1', 'img_url2', ...]")
    private List<StadiumImgDto.ImgResponse> imgs; // TODO: 이미지주소 + public_id도 포함 필요
    private List<String> tags;

    @ApiModelProperty(value = "오픈 시간", example = "HH:MM:SS")
    private Time openTime;

    @ApiModelProperty(value = "마감 시간", example = "HH:MM:SS")
    private Time closeTime;

    public static StadiumResponseDto fromEntity(Stadium stadium) {
        return StadiumResponseDto.builder()
                .id(stadium.getId())
                .lat(stadium.getLat())
                .lnt(stadium.getLnt())
                .address(stadium.getAddress())
                .star_avg(stadium.getStarAvg())
                .openTime(stadium.getOpenTime())
                .closeTime(stadium.getCloseTime())
//                // TODO: 찜하기 수 업데이트
//                .likes(stadium.getLikes().size())
                .imgs(stadium.getImgs().stream().map(img ->
                        StadiumImgDto.ImgResponse.builder()
                                .publicId(img.getImgId())
                                .imgUrl(img.getImgUrl())
                                .build())
                        .collect(Collectors.toList()))
                .tags(stadium.getTags().stream().map(StadiumTag::getName).collect(Collectors.toList()))
                .build();
    }
}
