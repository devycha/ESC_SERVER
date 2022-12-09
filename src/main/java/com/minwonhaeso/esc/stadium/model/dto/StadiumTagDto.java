package com.minwonhaeso.esc.stadium.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

public class StadiumTagDto {
    @Data
    @ApiModel(value = "체육관 종목(태그) 추가 Request Body")
    public static class AddTagRequest {
        private String tagName;
    }

    @Data
    @Builder
    @ApiModel(value = "체육관 종목(태그) 추가 Response Body")
    public static class AddTagResponse {
        private String tagName;
    }

    @Data
    @ApiModel(value = "체육관 종목(태그) 삭제 Request Body")
    public static class DeleteTagRequest {
        private String tagName;
    }
}
