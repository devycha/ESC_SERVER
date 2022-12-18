package com.minwonhaeso.esc.stadium.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

@ApiModel(value = "체육관 종목(태그) Body")
@Data
@Builder
public class StadiumTagDto {
    private Long id;
    private String tagName;
}
