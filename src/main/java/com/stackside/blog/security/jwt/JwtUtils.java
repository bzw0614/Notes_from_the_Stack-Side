package com.stackside.blog.security.jwt;

import com.stackside.blog.common.constant.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtUtils(@Value("${stackside.jwt.secret}") String secret,
                    @Value("${stackside.jwt.expiration-ms:86400000}") long expirationMillis) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = keyBytes.length >= 32 ? Keys.hmacShaKeyFor(keyBytes) : Keys.hmacShaKeyFor(padTo32Bytes(keyBytes));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.CLAIM_USER_ID, userId);
        claims.put(JwtConstants.CLAIM_USERNAME, username);
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        Object userId = parseToken(token).get(JwtConstants.CLAIM_USER_ID);
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(userId));
    }

    public String getUsername(String token) {
        return parseToken(token).get(JwtConstants.CLAIM_USERNAME, String.class);
    }

    private byte[] padTo32Bytes(byte[] source) {
        byte[] padded = new byte[32];
        for (int i = 0; i < padded.length; i++) {
            padded[i] = i < source.length ? source[i] : (byte) '_';
        }
        return padded;
    }
}
