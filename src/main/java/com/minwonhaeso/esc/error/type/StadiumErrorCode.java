package com.minwonhaeso.esc.error.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StadiumErrorCode {
    StadiumNotFound(HttpStatus.BAD_REQUEST, "일치하는 체육관 정보가 존재하지 않습니다."),
    ReservationNotFound(HttpStatus.BAD_REQUEST, "일치하는 예약 정보가 존재하지 않습니다."),
    ItemNotFound(HttpStatus.BAD_REQUEST, "일치하는 아이템 정보가 존재하지 않습니다."),
    StadiumReservationNotMatch(HttpStatus.BAD_REQUEST, "해당 체육관에 존재하는 예약이 아닙니다."),
    UnAuthorizedAccess(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다."),
    AlreadyReservedTime(HttpStatus.CONFLICT, "이미 예약이 완료되거나 진행중인 시간대입니다.");

    private final HttpStatus statusCode;
    private final String errorMessage;
}
