package com.minwonhaeso.esc.stadium.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "체육관 이미지 Body")
public class StadiumImgDto {
    private Long id;
    private String publicId;
    private String imgUrl;
}
