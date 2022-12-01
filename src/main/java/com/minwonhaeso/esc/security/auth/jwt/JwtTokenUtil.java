package com.minwonhaeso.esc.security.auth.jwt;

import com.minwonhaeso.esc.security.auth.redis.RefreshToken;
import com.minwonhaeso.esc.security.auth.redis.RefreshTokenRedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static com.minwonhaeso.esc.security.auth.jwt.JwtExpirationEnums.ACCESS_TOKEN_EXPIRATION_TIME;
import static com.minwonhaeso.esc.security.auth.jwt.JwtExpirationEnums.REFRESH_TOKEN_EXPIRATION_TIME;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    @Value("${spring.jwt.secret}")
    private String SECRET_KEY;

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public String generateAccessToken(String username) {
        return doGenerateToken(username, ACCESS_TOKEN_EXPIRATION_TIME.getValue());
    }

    public String generateRefreshToken(String username) {
        return doGenerateToken(username, REFRESH_TOKEN_EXPIRATION_TIME.getValue());
    }

    private String doGenerateToken(String email, long expireTime) { // 1
        Claims claims = Jwts.claims();
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsername(token);
        String details = userDetails.getUsername();
        return username.equals(details);
        //&& !isTokenExpired(token)
    }

    public long getRemainMilliSeconds(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        Date now = new Date();
        return expiration.getTime() - now.getTime();
    }

    public RefreshToken saveRefreshToken(String username) {
        return refreshTokenRedisRepository.save(RefreshToken.createRefreshToken(username,
                generateRefreshToken(username), REFRESH_TOKEN_EXPIRATION_TIME.getValue()));
    }
}