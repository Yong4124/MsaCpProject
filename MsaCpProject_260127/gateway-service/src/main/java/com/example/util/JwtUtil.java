package com.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // ⭐ 통일된 비밀 키 - 모든 서비스에서 동일하게 사용
    private static final String SECRET_KEY =
            "usfk-jwt-secret-codewave-recruitment-platform-shared-key-for-all-microservices-hs512";

    // ⭐ 통일된 만료 시간 (2시간)
    private static final long EXPIRATION_TIME = 7200000;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // JWT 토큰에서 정보 추출
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 로그인 ID 추출
    public String extractLoginId(String token) {
        return extractClaims(token).get("loginId", String.class);
    }

    // 회원 타입 추출 (PERSONAL, COMPANY, ADMIN)
    public String extractMemberType(String token) {
        return extractClaims(token).get("memberType", String.class);
    }

    // 토큰 유효성 검증
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("⚠️ JWT 검증 실패: " + e.getMessage());
            return false;
        }
    }

    // 토큰 만료 확인
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // Company 전용 - companyId 추출
    public Integer extractCompanyId(String token) {
        return extractClaims(token).get("companyId", Integer.class);
    }

    // Company 전용 - companyName 추출
    public String extractCompanyName(String token) {
        return extractClaims(token).get("companyName", String.class);
    }
}