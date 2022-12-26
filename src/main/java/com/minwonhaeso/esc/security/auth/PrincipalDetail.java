package com.minwonhaeso.esc.security.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minwonhaeso.esc.member.model.entity.Member;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PrincipalDetail implements UserDetails, OAuth2User {

    private String username;
    private String password;
    private Member member;
    private Map<String, Object> attributes;

    public PrincipalDetail(Member member) {
        this.username = member.getEmail();
        this.password = member.getPassword();
        this.member = member;
    }

    public PrincipalDetail(Member member, Map<String, Object> attributes) {
        this.username = member.getEmail();
        this.password = member.getPassword();
        this.attributes = attributes;
        this.member = member;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(this.member.getRole().name()));
    }
    public static UserDetails of(Member member) {
        return new PrincipalDetail(member);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
//    @JsonIgnore
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return null;
    }
}