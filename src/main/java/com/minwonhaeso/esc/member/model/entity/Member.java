package com.minwonhaeso.esc.member.model.entity;

import com.minwonhaeso.esc.member.model.type.MemberRole;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.minwonhaeso.esc.security.oauth2.type.ProviderType;
import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;

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
    @Column(name = "member_id")
    private Long memberId;

    @Column(unique = true, name = "email")
    private String email;

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private MemberType type;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType; // 소셜 타입

    @Column(name = "provider_id")
    private String providerId;
}
