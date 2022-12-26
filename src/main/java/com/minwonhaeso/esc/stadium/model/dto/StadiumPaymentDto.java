package com.minwonhaeso.esc.stadium.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class StadiumPaymentDto {

    @Data
    @Builder
    public static class PaymentRequest {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate date;
        private List<String> reservedTimes;
        private int headCount;
        private List<ItemRequest> items;
        private int totalPrice;
        private String email;
        private String paymentType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemRequest {
        private Long id;
        private int count;
    }
}
