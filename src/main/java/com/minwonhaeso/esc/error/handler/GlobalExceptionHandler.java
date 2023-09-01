package com.minwonhaeso.esc.error.handler;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.exception.NotificationException;
import com.minwonhaeso.esc.error.exception.ReviewException;
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
                .status(exception.getErrorCode().getStatusCode())
                .body(GlobalErrorResponse.from(exception.getErrorCode().getErrorMessage()));
    }

    @ExceptionHandler(AuthException.class)
    protected ResponseEntity<GlobalErrorResponse> handleAuthExceptionHandler(AuthException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getStatusCode())
                .body(GlobalErrorResponse.from(exception.getErrorCode().getErrorMessage()));
    }

    @ExceptionHandler(NotificationException.class)
    protected ResponseEntity<GlobalErrorResponse> handleNotificationExceptionHandler(NotificationException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getStatusCode())
                .body(GlobalErrorResponse.from(exception.getErrorCode().getErrorMessage()));
    }

    @ExceptionHandler(ReviewException.class)
    protected ResponseEntity<GlobalErrorResponse> handleReviewExceptionHandler(ReviewException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getStatusCode())
                .body(GlobalErrorResponse.from(exception.getErrorCode().getErrorMessage()));
    }
}