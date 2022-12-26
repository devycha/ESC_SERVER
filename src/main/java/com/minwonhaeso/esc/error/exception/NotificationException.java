package com.minwonhaeso.esc.error.exception;

import com.minwonhaeso.esc.error.type.NotificationErrorCode;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException{
    private final NotificationErrorCode errorCode;

    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
