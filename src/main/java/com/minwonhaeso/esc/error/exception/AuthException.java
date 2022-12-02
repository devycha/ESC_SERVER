package com.minwonhaeso.esc.error.exception;

import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{
    private final AuthErrorCode errorCode;

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
