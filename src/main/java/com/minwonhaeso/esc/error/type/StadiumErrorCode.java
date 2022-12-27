package com.minwonhaeso.esc.error.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StadiumErrorCode {
    StadiumNotFound(HttpStatus.BAD_REQUEST, "일치하는 체육관 정보가 존재하지 않습니다."),
    ReservationNotFound(HttpStatus.BAD_REQUEST, "일치하는 예약 정보가 존재하지 않습니다."),
    HasReservation(HttpStatus.BAD_REQUEST, "예약 정보가 존재합니다."),
    ItemNotFound(HttpStatus.BAD_REQUEST, "일치하는 아이템 정보가 존재하지 않습니다."),
    StadiumReservationNotMatch(HttpStatus.BAD_REQUEST, "해당 체육관에 존재하는 예약이 아닙니다."),
    UnAuthorizedAccess(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    AlreadyReservedTime(HttpStatus.CONFLICT, "이미 예약이 완료되거나 진행중인 시간대입니다."),
    LikeRequestAlreadyMatched(HttpStatus.BAD_REQUEST, "요청 타입이 이미 적용되어 있습니다."),
    TimeFormatNotAccepted(HttpStatus.BAD_REQUEST, "시간 형식이 옳바르지 않습니다."),
    CouldNotCancelReservation(HttpStatus.BAD_REQUEST, "취소할 수 없는 예약입니다."),
    TooEarlyExecute(HttpStatus.BAD_REQUEST, "사용 완료할 수 없는 날짜입니다."),
    StadiumImgNotFound(HttpStatus.BAD_REQUEST, "일치하는 체육관 이미지 정보가 존재하지 않습니다."),
    StadiumTagNotFound(HttpStatus.BAD_REQUEST, "일치하는 체육관 종목 정보가 존재하지 않습니다."),
    StadiumItemNotFound(HttpStatus.BAD_REQUEST, "일치하는 체육관 대여 용품 정보가 존재하지 않습니다."),
    LatLntInvalid(HttpStatus.BAD_REQUEST, "위도 경도 값이 옳바르지 않습니다.");

    private final HttpStatus statusCode;
    private final String errorMessage;
}