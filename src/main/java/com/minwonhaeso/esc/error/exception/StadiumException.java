package com.minwonhaeso.esc.error.exception;

import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import lombok.Getter;

@Getter
public class StadiumException extends RuntimeException{
    private final StadiumErrorCode errorCode;

    public StadiumException(StadiumErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
