package com.minwonhaeso.esc.common.exception.stadium.handler;

import com.minwonhaeso.esc.common.exception.CustomException;
import com.minwonhaeso.esc.common.exception.stadium.StadiumNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class StadiumExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> stadiumNotFoundExceptionHandler(StadiumNotFoundException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(exception.getMessage());
    }
}
