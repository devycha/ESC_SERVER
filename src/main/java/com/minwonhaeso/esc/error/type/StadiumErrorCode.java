package com.minwonhaeso.esc.error.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StadiumErrorCode {
    StadiumNotFound(HttpStatus.BAD_REQUEST, "일치하는 체육관 정보가 존재하지 않습니다."),
    UnAuthorizedAccess(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다."),
    LikeRequestAlreadyMatched(HttpStatus.BAD_REQUEST, "요청 타입이 이미 적용되어 있습니다.");

    private final HttpStatus statusCode;
    private final String errorMessage;
}
