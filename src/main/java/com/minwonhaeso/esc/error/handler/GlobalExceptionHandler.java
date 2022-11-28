package com.minwonhaeso.esc.error.handler;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.exception.StadiumException;
import com.minwonhaeso.esc.error.response.GlobalErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StadiumException.class)
    protected ResponseEntity<GlobalErrorResponse> handleStadiumExceptionHandler(StadiumException exception) {
        return ResponseEntity
                .status(exception.getErrorcode().getStatusCode())
                .body(GlobalErrorResponse.from(exception.getErrorcode().getErrorMessage()));
    }


    @ExceptionHandler(AuthException.class)
    protected ResponseEntity<GlobalErrorResponse> handleStadiumExceptionHandler(AuthException exception) {
        return ResponseEntity
                .status(exception.getErrorcode().getStatusCode())
                .body(GlobalErrorResponse.from(exception.getErrorcode().getErrorMessage()));
    }
}
