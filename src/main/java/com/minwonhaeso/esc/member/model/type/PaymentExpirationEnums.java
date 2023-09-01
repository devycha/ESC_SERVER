package com.minwonhaeso.esc.member.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentExpirationEnums {

    PAYMENT_ACCESS_TIME("Payment 만료 시간 / 10분", 1000L * 60 * 10);

    private String description;
    private Long value;
}
