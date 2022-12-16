package com.minwonhaeso.esc.error.exception;

import com.minwonhaeso.esc.error.type.ReviewErrorCode;
import com.minwonhaeso.esc.error.type.StadiumErrorCode;
import lombok.Getter;

@Getter
public class ReviewException extends RuntimeException{
    private final ReviewErrorCode errorCode;

    public ReviewException(ReviewErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
