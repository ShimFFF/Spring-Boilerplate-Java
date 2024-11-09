package com.example.goormthon_univ_3th.global.config.security.jwt;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.refreshExpiration}")
    private long jwtAccessExpiration;

    @Value("${jwt.accessExpiration}")
    private long jwtRefreshExpiration;


    private Key key;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
        this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateToken(String memberId, String subject, TokenType tokenType) {
        Date now = new Date();
        Date expiration;
        // 분기 나눠야 ㅐ-해, 리프레쉬 토큰과 액세스 토큰의 만료시간이 다르니까
        if (TokenType.ACCESS.equals(tokenType)) { // 액세스 토큰
            expiration=calculateExpirationDate(now, jwtAccessExpiration);
        } else { // 리프레쉬 토큰
            expiration=calculateExpirationDate(now, jwtRefreshExpiration);
        }

        Claims claims = Jwts.claims().setSubject(subject); // JWT payload 에 저장되는 정보단위
        claims.put("memberId",  memberId);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    public TokenInfo generateToken(String memberId, String subject) {
        String accessToken = generateToken(memberId, subject, TokenType.ACCESS);
        String refreshToken = generateToken(memberId, subject, TokenType.REFRESH);

        return new TokenInfo(accessToken, refreshToken);
    }

    private Date calculateExpirationDate(Date createdDate, long jwtExpiration) {
        return new Date(createdDate.getTime() + jwtExpiration);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 유효하지 않은 토큰 처리
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}

