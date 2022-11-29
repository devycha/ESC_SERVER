package com.minwonhaeso.esc.stadium.dto;

import com.minwonhaeso.esc.stadium.entity.Stadium;
import com.minwonhaeso.esc.stadium.entity.StadiumImg;
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
public class StadiumResponseDto {
    private Long id;
    private Double lat;
    private Double lnt;
    private String address;
    private Double star_avg;
    private Integer likes;
    private List<String> imgs;
    private List<String> tags;

    public static StadiumResponseDto fromEntity(Stadium stadium) {
        return StadiumResponseDto.builder()
                .id(stadium.getId())
                .lat(stadium.getLat())
                .lnt(stadium.getLnt())
                .address(stadium.getAddress())
                .star_avg(stadium.getStarAvg())
//                // TODO: 찜하기 수 업데이트
//                .likes(stadium.getLikes().size())
//
                .imgs(stadium.getImgs().stream().map(StadiumImg::getImgUrl)
                        .collect(Collectors.toList()))
//
//                // TODO: 태그 데이터 문자 리스트로 업데이트
//                .tags(stadium.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .build();
    }
}
