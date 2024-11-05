package com.example.goormthon_univ_3th.global.config.security.jwt;

import com.example.goormthon_univ_3th.global.common.exception.RestApiException;
import com.example.goormthon_univ_3th.global.common.exception.code.status.AuthErrorStatus;
import com.example.goormthon_univ_3th.global.config.security.auth.PrincipalDetailsService;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.jwt-key}")
    private String jwtSecretKey;

    @Value("${jwt.refresh-key}")
    private String refreshSecretKey;

    private final PrincipalDetailsService principalDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REFRESH_HEADER = "refreshToken";
    private static final long TOKEN_VALID_TIME = 1000 * 60L * 60L * 24L;  // 1일 유효기간
    private static final long REF_TOKEN_VALID_TIME = 1000 * 60L * 60L * 24L * 14L;  // 14일 유효기간
    private Key key;
    private Key refreshKey;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
        this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());

        byte[] refreshKeyBytes = Base64.getDecoder().decode(refreshSecretKey);
        this.refreshKey = new SecretKeySpec(refreshKeyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateAccessToken(Long memberId, String role) {
        Date now = new Date();
        Date accessTokenExpirationTime = new Date(now.getTime() + TOKEN_VALID_TIME);

        Claims claims = Jwts.claims().setSubject(memberId.toString());
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(accessTokenExpirationTime)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long memberId) {
        Date now = new Date();
        Date refreshTokenExpirationTime = new Date(now.getTime() + REF_TOKEN_VALID_TIME);

        Claims claims = Jwts.claims().setSubject(memberId.toString());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(refreshTokenExpirationTime)
                .signWith(refreshKey)
                .compact();
    }

    public TokenInfo generateToken(Long memberId, String role) {
        String accessToken = generateAccessToken(memberId, role);
        String refreshToken = generateRefreshToken(memberId);
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String role = claims.get("role", String.class);

        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        UserDetails userDetails = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new RestApiException(AuthErrorStatus.INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new RestApiException(AuthErrorStatus.EXPIRED_MEMBER_JWT);
        } catch (UnsupportedJwtException | SignatureException e) {
            throw new RestApiException(AuthErrorStatus.UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            throw new RestApiException(AuthErrorStatus.EMPTY_JWT);
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new RestApiException(AuthErrorStatus.INVALID_REFRESH_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new RestApiException(AuthErrorStatus.EXPIRED_MEMBER_JWT);
        } catch (UnsupportedJwtException | SignatureException e) {
            throw new RestApiException(AuthErrorStatus.UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            throw new RestApiException(AuthErrorStatus.EMPTY_JWT);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_HEADER);
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return request.getHeader(REFRESH_HEADER);
    }
}

