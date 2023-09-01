package com.minwonhaeso.esc.error.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GlobalErrorResponse {
    private final LocalDateTime timestamp;
    private final boolean result;
    private final String errorMessage;

    public static GlobalErrorResponse from(String errorMessage) {
        return GlobalErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .result(false)
                .errorMessage(errorMessage)
                .build();
    }
}
