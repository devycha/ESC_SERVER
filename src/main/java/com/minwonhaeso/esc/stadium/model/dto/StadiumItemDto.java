package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.StadiumItem;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class StadiumItemDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(value = "체육관 대여 용품 추가 Request Body")
    public static class CreateItemRequest {
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateItemRequest {
        private Long id;
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;
    }

    @Data
    @Builder
    @ApiModel(value = "체육관 대여 용품 추가 성공 Response Body")
    public static class CreateItemResponse {
        private Long id;
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;
    }

    @Data
    @ApiModel(value = "체육관 아이템 삭제 Request Body")
    public static class DeleteItemRequest {
        private Long id;
    }

    @Data
    @Builder
    @ApiModel(value = "체육관 대여 용품 Response Body")
    public static class Response {
        private Long id;
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;

        public static Response fromEntity(StadiumItem item) {
            return Response.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .publicId(item.getImgId())
                    .imgUrl(item.getImgUrl())
                    .price(item.getPrice())
                    .build();
        }
    }
}
