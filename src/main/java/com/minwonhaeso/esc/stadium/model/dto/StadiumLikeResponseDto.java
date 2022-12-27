package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.Stadium;
import com.minwonhaeso.esc.stadium.model.entity.StadiumLike;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.minwonhaeso.esc.stadium.model.type.StadiumStatus.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StadiumLikeResponseDto {
    private Long stadiumId;

    private String name;

    private String address;

    private Double starAvg;

    private String imgUrl;


    public static StadiumLikeResponseDto fromEntity(StadiumLike stadiumLike) {
        Stadium stadium = stadiumLike.getStadium();
        if (stadium.getStatus() == DELETED
                || stadium.getStatus() == BANNED) {
            return null;
        }
        return StadiumLikeResponseDto.builder()
                .stadiumId(stadium.getId())
                .name(stadium.getName())
                .imgUrl(stadium.getImgs().get(0).getImgUrl())
                .address(stadium.getAddress())
                .starAvg(stadium.getStarAvg())
                .build();
    }
}