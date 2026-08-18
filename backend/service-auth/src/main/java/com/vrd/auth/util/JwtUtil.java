/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.jsonwebtoken.Claims
 *  io.jsonwebtoken.Jwts
 *  io.jsonwebtoken.security.Keys
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.stereotype.Component
 */
package com.vrd.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    @Value(value="${jwt.secret}")
    private String secret;
    @Value(value="${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor((byte[])this.secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Long userId) {
        HashMap<String, Object> claims = new HashMap<String, Object>();
        claims.put("userId", userId);
        claims.put("username", username);
        return this.createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + this.expiration);
        return Jwts.builder().claims(claims).subject(subject).issuedAt(now).expiration(expirationDate).signWith((Key)this.getSigningKey()).compact();
    }

    public Long getExpiration() {
        return this.expiration;
    }

    public String getUsernameFromToken(String token) {
        return this.getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = this.getClaimsFromToken(token);
        return (Long)claims.get("userId", Long.class);
    }

    public Date getExpirationDateFromToken(String token) {
        return this.getClaimsFromToken(token).getExpiration();
    }

    private Claims getClaimsFromToken(String token) {
        return (Claims)Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims((CharSequence)token).getPayload();
    }

    public Boolean validateToken(String token) {
        try {
            Date expiration = this.getExpirationDateFromToken(token);
            return !expiration.before(new Date());
        }
        catch (Exception e) {
            return false;
        }
    }

    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = this.getExpirationDateFromToken(token);
            return expiration.before(new Date());
        }
        catch (Exception e) {
            return true;
        }
    }
}

