package com.minwonhaeso.esc.stadium.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class StadiumPaymentDto {

    @Data
    public static class PaymentConfirmRequest{
        private LocalDate date;
        private String openTime;
        private String closeTime;
        private int pricePerHalfHour;
        private int headCount;
        private List<StadiumReservationDto.ItemResponse> items;
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
}
