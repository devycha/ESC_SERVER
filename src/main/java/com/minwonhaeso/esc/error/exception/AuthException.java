package com.minwonhaeso.esc.error.exception;

import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{
    private final StadiumErrorCode errorcode;

    public AuthException(StadiumErrorCode errorcode) {
        super(errorcode.getErrorMessage());
        this.errorcode = errorcode;
    }
}
