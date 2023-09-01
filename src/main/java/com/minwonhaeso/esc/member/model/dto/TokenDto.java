package com.minwonhaeso.esc.member.model.dto;

import lombok.*;

import static com.minwonhaeso.esc.security.auth.jwt.JwtHeaderUtilEnums.GRANT_TYPE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;

    public static TokenDto of(String accessToken, String refreshToken) {
        return TokenDto.builder()
                .grantType(GRANT_TYPE.getValue())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    public static TokenDto of(String accessToken) {
        return TokenDto.builder()
                .grantType(GRANT_TYPE.getValue())
                .accessToken(accessToken)
                .build();
    }
}
