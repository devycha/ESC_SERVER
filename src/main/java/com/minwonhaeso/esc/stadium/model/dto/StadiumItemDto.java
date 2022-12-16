package com.minwonhaeso.esc.stadium.model.dto;

import com.minwonhaeso.esc.stadium.model.entity.StadiumItem;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StadiumItemDto {
    @Data
    @ApiModel(value = "체육관 대여 용품 추가 Request Body")
    public static class CreateItemRequest {
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;
    }

    @Data
    @Builder
    @ApiModel(value = "체육관 대여 용품 추가 성공 Response Body")
    public static class CreateItemResponse {
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;
        private boolean isAvailable;
    }

    @Data
    @ApiModel(value = "체육관 아이템 삭제 Request Body")
    public static class DeleteItemRequest {
        private Long itemId;
    }

    @Data
    @Builder
    @ApiModel(value = "체육관 대여 용품 Response Body")
    public static class Response {
        private String name;
        private String publicId;
        private String imgUrl;
        private Integer price;
        private boolean isAvailable;

        public static Response fromEntity(StadiumItem item) {
            return Response.builder()
                    .name(item.getName())
                    .publicId(item.getImgId())
                    .imgUrl(item.getImgUrl())
                    .price(item.getPrice())
                    .build();
        }
    }
}
