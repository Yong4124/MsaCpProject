package com.example.company.util;

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

    // 비밀 키 (256비트 이상) - 실제 운영에서는 application.properties에서 관리
    private static final String SECRET_KEY =
            "usfk-jwt-secret-codewave-recruitment-platform-shared-key-for-all-microservices-hs512";
    private static final long EXPIRATION_TIME = 7200000; // 24시간

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // JWT 토큰 생성
    public String generateToken(String loginId, String memberType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("loginId", loginId);
        claims.put("memberType", memberType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(loginId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
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

    // 회원 타입 추출
    public String extractMemberType(String token) {
        return extractClaims(token).get("memberType", String.class);
    }

    // 토큰 유효성 검증
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰 만료 확인
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // ✅ 기업 로그인 전용 JWT 생성
    public String generateCompanyToken(String loginId, Integer companyId, String companyName,
                                       String ceoName, String companyAddress, String logoPath) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("loginId", loginId);
        claims.put("memberType", "COMPANY");
        claims.put("companyId", companyId);
        claims.put("companyName", companyName);
        claims.put("ceoName", ceoName);
        claims.put("companyAddress", companyAddress);
        claims.put("logoPath", logoPath);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(loginId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
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