package com.minwonhaeso.esc.stadium.model.dto;

import lombok.Builder;
import lombok.Data;

public class CreateStadiumItemDto {
    @Data
    public static class Request {
        private String name;
        private String imgUrl;
        private Integer price;
        private Integer cnt;
    }

    @Data
    @Builder
    public static class Response {
        private String name;
        private String imgUrl;
        private Integer price;
        private Integer cnt;
        private boolean isAvailable;
    }
}
