package com.minwonhaeso.esc.stadium.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "stadiums")
public class StadiumDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Double)
    private Double lat;

    @Field(type = FieldType.Double)
    private Double lnt;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Double)
    private Double starAvg;

    @Field(type = FieldType.Integer)
    private Integer weekdayPricePerHalfHour;

    @Field(type = FieldType.Integer)
    private Integer holidayPricePerHalfHour;

    @Field(type = FieldType.Text)
    private String img;

    @Field(type = FieldType.Text)
    private List<String> tags;

    public static StadiumDocument fromEntity(Stadium stadium) {
        return StadiumDocument.builder()
                .id(stadium.getId())
                .name(stadium.getName())
                .address(stadium.getAddress())
                .lat(stadium.getLat())
                .lnt(stadium.getLnt())
                .starAvg(stadium.getStarAvg())
                .weekdayPricePerHalfHour(stadium.getWeekdayPricePerHalfHour())
                .holidayPricePerHalfHour(stadium.getHolidayPricePerHalfHour())
                .img(stadium.getImgs().isEmpty() ?
                        null :
                        stadium.getImgs().get(0).getImgUrl())
                .tags(stadium.getTags().stream().map(StadiumTag::getName).collect(Collectors.toList()))
                // TODO: 찜하기 수 업데이트
                // .likes(stadium.getLikes().size())
                .build();
    }
}
