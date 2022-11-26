package com.minwonhaeso.esc.member.model.dto;

import com.minwonhaeso.esc.member.model.type.MemberType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignDto {
    private MemberType type;
    private String email;
    private String name;
    private String password;
    private String nickname;
}
