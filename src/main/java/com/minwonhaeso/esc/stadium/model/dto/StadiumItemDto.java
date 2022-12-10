package com.minwonhaeso.esc.stadium.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

public class StadiumItemDto {
    @Data
    @ApiModel(value = "체육관 대여 용품 추가 Request Body")
    public static class CreateItemRequest {
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;
        private Integer cnt;
    }

    @Data
    @Builder
    @ApiModel(value = "체육관 대여 용품 추가 성공 Response Body")
    public static class CreateItemResponse {
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;
        private Integer cnt;
        private boolean isAvailable;
    }

    @Data
    @ApiModel(value = "체육관 아이템 삭제 Request Body")
    public static class DeleteItemRequest {
        private Long itemId;
    }
}
