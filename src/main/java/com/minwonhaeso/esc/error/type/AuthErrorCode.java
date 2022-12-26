package com.minwonhaeso.esc.error.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {
    MemberNotFound(HttpStatus.NOT_FOUND,"없는 회원 정보입니다."),
    EmailAuthNotYet(HttpStatus.BAD_REQUEST, "이메일 인증을 먼저 해주세요."),
    EmailAlreadySignUp(HttpStatus.BAD_REQUEST, "사용할 수 없는 이메일입니다."),
    MemberTypeNotMatch(HttpStatus.BAD_REQUEST, "다른 타입으로 로그인해주세요."),
    EmailAuthTimeOut(HttpStatus.CONFLICT, "인증 시간이 만료되었습니다. 다시 인증해주세요."),
    EmailNotMatched(HttpStatus.BAD_REQUEST, "이메일이 정확하지 않습니다."),
    PasswordNotEqual(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    MemberNotLogIn(HttpStatus.UNAUTHORIZED, "로그인을 해주세요."),
    AccessTokenAlreadyExpired(HttpStatus.UNAUTHORIZED, "인증이 만료되었습니다. 다시 로그인해주세요."),
    TokenNotMatch(HttpStatus.CONFLICT, "토큰이 일치하지 않습니다."),
    AuthKeyNotMatch(HttpStatus.CONFLICT, "인증 코드가 맞지 않습니다."),
    OAuthProviderMissMatch(HttpStatus.BAD_REQUEST, "기존에 회원가입한 소셜 타입과 일치하지 않습니다.");

    private final HttpStatus statusCode;
    private final String errorMessage;
}
