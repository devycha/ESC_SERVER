package com.minwonhaeso.esc.stadium.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

public class StadiumImgDto {
    @Data
    @Builder
    @ApiModel(value = "체육관 이미지 조회 Response Body")
    public static class ImgResponse {
        private String publicId;
        private String imgUrl;
    }

    @Data
    @ApiModel(value = "체육관 이미지 추가 Request Body")
    public static class CreateImgRequest {
        private String publicId;
        private String imgUrl;
    }

    @Data
    @Builder
    @ApiModel(value = "체육관 이미지 Response Body")
    public static class CreateImgResponse {
        private String publicId;
        private String imgUrl;
    }

    @Data
    @ApiModel(value = "체육관 이미지 추가 Request Body")
    public static class AddImgRequest {
        private String publicId;
        private String imgUrl;
    }

    @Data
    @ApiModel(value = "체육관 이미지 삭제 Request Body")
    public static class DeleteImgRequest {
        private String publicId;
        private String imgUrl;
    }
}
