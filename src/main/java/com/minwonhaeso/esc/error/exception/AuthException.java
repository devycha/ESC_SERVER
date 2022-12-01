package com.minwonhaeso.esc.error.exception;

import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{
    private final AuthErrorCode errorcode;

    public AuthException(AuthErrorCode errorcode) {
        super(errorcode.getErrorMessage());
        this.errorcode = errorcode;
    }
}
