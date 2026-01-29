package com.example.job.util;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

@Component
public class JwtUtil {

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

    public Integer extractCompanyId(String token) {
        return extractClaims(token).get("companyId", Integer.class);
    }

    public String extractCompanyName(String token) {
        return extractClaims(token).get("companyName", String.class);
    }

    public String extractCeoName(String token) {
        return extractClaims(token).get("ceoName", String.class);
    }

    public String extractCompanyAddress(String token) {
        return extractClaims(token).get("companyAddress", String.class);
    }

    public String extractLogoPath(String token) {
        return extractClaims(token).get("logoPath", String.class);
    }
}

