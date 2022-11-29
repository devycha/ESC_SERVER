package com.minwonhaeso.esc.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    private final ErrorCode errorcode;

    public CustomException(ErrorCode errorcode) {
        super(errorcode.getErrorMessage());
        this.errorcode = errorcode;
    }
}
