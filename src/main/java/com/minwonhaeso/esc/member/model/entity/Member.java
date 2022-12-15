package com.minwonhaeso.esc.member.model.entity;

import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.type.MemberRole;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import com.minwonhaeso.esc.stadium.model.entity.StadiumLike;
import com.sun.istack.NotNull;
import io.jsonwebtoken.Claims;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "member")
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private String imgUrl;

    @Column(unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private MemberType type;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType; // 소셜 타입

    private String providerId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "likes")
    private List<StadiumLike> likes = new ArrayList<>();


    public static Member of(SignDto.Request signDto) {
        Member member = Member.builder()
                .email(signDto.getEmail())
                .name(signDto.getName())
                .password(signDto.getPassword())
                .status(MemberStatus.ING)
                .nickname(signDto.getNickname())
                .imgUrl(signDto.getImage())
                .providerType(ProviderType.LOCAL)
                .providerId(signDto.getEmail().split("@")[0])
                .build();
        if (signDto.getType().equals(MemberType.USER.name())) {
            member.setType(MemberType.USER);
            member.setRole(MemberRole.ROLE_USER);
        } else {
            member.setType(MemberType.MANAGER);
            member.setRole(MemberRole.ROLE_STADIUM);
        }
        return member;
    }

    public Member(Claims claims) {
        this.memberId = Long.valueOf(claims.get("userId").toString());
        this.name = claims.get("name").toString();
        this.role = MemberRole.valueOf(claims.get("role").toString());
    }

}
