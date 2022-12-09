package com.minwonhaeso.esc.member.service;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.repository.MemberRepository;
import com.minwonhaeso.esc.security.auth.PrincipalDetail;
import com.minwonhaeso.esc.security.auth.redis.CacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.minwonhaeso.esc.error.type.AuthErrorCode.MemberNotFound;

@Service
@RequiredArgsConstructor
public class CustomerMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Cacheable(value = CacheKey.USER, key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(email);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new AuthException(MemberNotFound));
        return PrincipalDetail.of(member);
    }
}