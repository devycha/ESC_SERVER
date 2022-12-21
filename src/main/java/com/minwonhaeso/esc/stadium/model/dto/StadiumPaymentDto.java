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
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentConfirmRequest{
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate date;
        private String openTime;
        private String closeTime;
        private int pricePerHalfHour;
        private int headCount;
        private List<ItemRequest> items;
        private List<String> reservedTimes;

    }
    @Data
    @Builder
    public static class PaymentConfirmResponse{
        private Long stadiumId;
        private String name;
        private LocalDate date;
        private String openTime;
        private String closeTime;
        private int headCount;
        private int price;
    }

    @Data
    public static class PaymentRequest {
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
