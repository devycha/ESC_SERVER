package com.minwonhaeso.esc.security.auth.jwt;

import com.minwonhaeso.esc.error.exception.AuthException;
import com.minwonhaeso.esc.error.type.AuthErrorCode;
import com.minwonhaeso.esc.member.model.entity.Member;
import com.minwonhaeso.esc.member.service.CustomerMemberDetailsService;
import com.minwonhaeso.esc.security.auth.redis.LogoutAccessTokenRedisRepository;
import com.minwonhaeso.esc.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.minwonhaeso.esc.error.type.AuthErrorCode.AccessTokenAlreadyExpired;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomerMemberDetailsService customerMemberDetailsService;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
    private final String[] baseUrl = {
            "/members/auth/login",
            "/members/auth/refresh-token",
            "/stadiums",
            "/stadiums/near-loc",
            "/members/profiles/password/config"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return Arrays.stream(baseUrl).anyMatch(url -> url.equalsIgnoreCase(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, AuthException {
        String accessToken = getToken(request);
        if (accessToken != null) {
            checkLogout(accessToken);
            try {
                String username = jwtTokenUtil.getUsername(accessToken);

                if (username != null) {
                    UserDetails userDetails = customerMemberDetailsService.loadUserByUsername(username);
                    validateAccessToken(accessToken, userDetails);
                    processSecurity(request, userDetails);
                }

            } catch (ExpiredJwtException e) {
                response.sendError(AccessTokenAlreadyExpired.getStatusCode().value(),
                        AccessTokenAlreadyExpired.getErrorMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
    private Authentication getAuthentication(HttpServletRequest request) throws AuthException {

        String token = request.getHeader("Authorization");

        if (token == null) {
            return null;
        }

        Claims claims;

        try {
            claims = jwtTokenUtil.extractAllClaims(token.substring("Bearer ".length()));
        } catch (JwtException e) {
            throw new AuthException(AccessTokenAlreadyExpired);
        }

        Set<GrantedAuthority> roles = new HashSet<>();
        String role = (String) claims.get("role");
        roles.add(new SimpleGrantedAuthority("ROLE_" + role));

        return new UsernamePasswordAuthenticationToken(new Member(claims), null, roles);
    }

    private String getToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    private void checkLogout(String accessToken) {
        if (logoutAccessTokenRedisRepository.existsById(accessToken)) {
            throw new IllegalArgumentException("이미 로그아웃된 회원입니다.");
        }
    }

    private void equalsUsernameFromTokenAndUserDetails(String userDetailsUsername, String tokenUsername) {
        if (!userDetailsUsername.equals(tokenUsername)) {
            throw new IllegalArgumentException("name이 토큰과 맞지 않습니다.");
        }
    }

    private void validateAccessToken(String accessToken, UserDetails userDetails) {
        if (!jwtTokenUtil.validateToken(accessToken, userDetails)) {
            throw new IllegalArgumentException("토큰 검증 실패");
        }
    }

    private void processSecurity(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }
}