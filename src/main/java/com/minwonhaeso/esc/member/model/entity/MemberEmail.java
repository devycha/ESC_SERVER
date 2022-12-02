package com.minwonhaeso.esc.member.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Id;

@Getter
@Setter
@RedisHash("memberEmail")
@AllArgsConstructor
@Builder
public class MemberEmail {

    // authKey
    @Id
    private String id;

    private String email;

    private boolean authYn;

    @TimeToLive
    private Long expireDt;

    public static MemberEmail createEmailAuthKey(String email, String authKey, Long emailExpiredTime){
        return MemberEmail.builder()
                .id(authKey)
                .email(email)
                .expireDt(emailExpiredTime)
                .authYn(false)
                .build();
    }

}
