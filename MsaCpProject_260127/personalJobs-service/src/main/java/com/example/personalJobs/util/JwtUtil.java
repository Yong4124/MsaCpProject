package com.example.personalJobs.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // ✅ personal-service랑 반드시 동일해야 함
    private static final String SECRET_KEY =
            "usfk-jwt-secret-codewave-recruitment-platform-shared-key-for-all-microservices-hs512";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractLoginId(String token) {
        return extractClaims(token).get("loginId", String.class);
    }

    public String extractMemberType(String token) {
        return extractClaims(token).get("memberType", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims c = extractClaims(token);
            Date exp = c.getExpiration();
            return exp == null || exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
