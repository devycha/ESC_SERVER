package com.minwonhaeso.esc.stadium.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "체육관 종목(태그) Body")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StadiumTagDto {
    private Long id;
    private String tagName;
}
