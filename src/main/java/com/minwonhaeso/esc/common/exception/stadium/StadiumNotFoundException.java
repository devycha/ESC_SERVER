package com.minwonhaeso.esc.common.exception.stadium;

import com.minwonhaeso.esc.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class StadiumNotFoundException extends CustomException {
    public StadiumNotFoundException(HttpStatus statusCode, String message) {
        super(statusCode, message);
    }
}
