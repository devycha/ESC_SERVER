package com.minwonhaeso.esc.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException{
    private final HttpStatus statusCode;

    public CustomException(HttpStatus statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
