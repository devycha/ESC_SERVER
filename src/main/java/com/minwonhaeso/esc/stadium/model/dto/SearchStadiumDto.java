package com.minwonhaeso.esc.stadium.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SearchStadiumDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "체육관 검색 Request Body")
    public static class Request {
        private String searchValue;
    }
}
