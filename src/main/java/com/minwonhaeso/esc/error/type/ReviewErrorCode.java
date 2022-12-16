package com.minwonhaeso.esc.error.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode {
    ReviewNotFound(HttpStatus.BAD_REQUEST, "일치하는 리뷰 정보가 존재하지 않습니다."),
    ReviewCountOverReservation(HttpStatus.BAD_REQUEST, "입력 가능한 모든 리뷰를 작성하였습니다."),
    NoReservationForReview(HttpStatus.BAD_REQUEST, "예약(사용) 내역이 존재하지 않습니다."),
    UnAuthorizedAccess(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다.");

    private final HttpStatus statusCode;
    private final String errorMessage;
}
