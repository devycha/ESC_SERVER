package com.minwonhaeso.esc.error.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode {
    UnAuthorizedAccess(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    private final HttpStatus statusCode;
    private final String errorMessage;
}
