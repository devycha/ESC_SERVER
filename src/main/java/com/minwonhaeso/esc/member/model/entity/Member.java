package com.minwonhaeso.esc.member.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minwonhaeso.esc.member.model.dto.SignDto;
import com.minwonhaeso.esc.member.model.type.MemberRole;
import com.minwonhaeso.esc.member.model.type.MemberStatus;
import com.minwonhaeso.esc.member.model.type.MemberType;
import com.sun.istack.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;


@Entity
@Table(name = "member")
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member implements UserDetails {
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



    public static UserDetails of(Member member) {
        return member.builder()
                .name(member.getUsername())
                .password(member.getPassword())
                .role(member.getRole())
                .build();
    }

    public static Member of(SignDto signDto) {
        Member member =  Member.builder()
                .email(signDto.getEmail())
                .name(signDto.getName())
                .password(signDto.getPassword())
                .nickname(signDto.getNickname())
                .imgUrl(signDto.getImgUrl())
                .type(signDto.getType())
                .build();
        if(signDto.getType() == MemberType.ADMIN){
            member.role = MemberRole.ROLE_STADIUM;
        }else{
            member.role = MemberRole.ROLE_USER;
        }
        return member;
    }
    //    @OneToMany(mappedBy = "member")
//    private List<Stadidum> stadiums;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return false;
    }


}
