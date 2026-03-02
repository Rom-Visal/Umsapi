package com.example.rolebase.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtils {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String accessSecret;

    @Value("${jwt.refresh-secret:${jwt.secret}}")
    private String refreshSecret;

    @Getter
    @Value("${jwt.expiration-ms:900000}")
    private long accessTokenExpirationMs;

    @Getter
    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), accessTokenExpirationMs, getAccessSigningKey(),
                ACCESS_TOKEN_TYPE, UUID.randomUUID().toString());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), refreshTokenExpirationMs, getRefreshSigningKey(),
                REFRESH_TOKEN_TYPE, UUID.randomUUID().toString());
    }

    public String getUsernameFromAccessToken(String token) {
        return getClaims(token, getAccessSigningKey()).getSubject();
    }

    public String getUsernameFromRefreshToken(String token) {
        return getClaims(token, getRefreshSigningKey()).getSubject();
    }

    public Date getRefreshTokenExpiration(String token) {
        return getClaims(token, getRefreshSigningKey()).getExpiration();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, getAccessSigningKey(), ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, getRefreshSigningKey(), REFRESH_TOKEN_TYPE);
    }

    private String buildToken(String username, long expirationMs, SecretKey signingKey,
                              String tokenType, String jti) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        Map<String, Object> claims = new HashMap<>();
        claims.put(TYPE_CLAIM, tokenType);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .id(jti)
                .signWith(signingKey)
                .compact();
    }

    private boolean validateToken(String token, SecretKey key, String expectedType) throws io.jsonwebtoken.ExpiredJwtException, io.jsonwebtoken.MalformedJwtException {
        try {
            Claims claims = getClaims(token, key);
            Object tokenType = claims.get(TYPE_CLAIM);
            return expectedType.equals(tokenType);
        } catch (SecurityException ex) {
            log.warn("Invalid JWT signature");
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty");
        }
        return false;
    }

    private Claims getClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getAccessSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(accessSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getRefreshSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
