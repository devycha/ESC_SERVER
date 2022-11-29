package com.minwonhaeso.esc.common.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final HttpStatus statusCode;
    private final String errorMessage;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorcode) {
        return ResponseEntity
                .status(errorcode.getStatusCode())
                .body(ErrorResponse.builder()
                        .statusCode(errorcode.getStatusCode())
                        .errorMessage(errorcode.getErrorMessage())
                        .build());
    }
}
