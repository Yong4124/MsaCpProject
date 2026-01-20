package com.example.company.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 비밀번호 재설정 토큰 서비스
 */
@Service
public class PasswordResetTokenService {

    // 토큰 저장 (토큰 -> {loginId, 만료시간})
    private final Map<String, ResetToken> tokens = new ConcurrentHashMap<>();

    private static final int EXPIRATION_MINUTES = 30;

    /**
     * 토큰 생성
     */
    public String createToken(String loginId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        tokens.put(token, new ResetToken(loginId, expiresAt));

        System.out.println("========================================");
        System.out.println("[비밀번호 재설정] 토큰: " + token);
        System.out.println("[비밀번호 재설정] 사용자: " + loginId);
        System.out.println("[비밀번호 재설정] 만료시간: " + expiresAt);
        System.out.println("========================================");

        return token;
    }

    /**
     * 토큰 검증 및 loginId 반환
     */
    public String validateToken(String token) {
        ResetToken stored = tokens.get(token);

        if (stored == null) {
            return null;
        }

        // 만료 체크
        if (LocalDateTime.now().isAfter(stored.expiresAt)) {
            tokens.remove(token);
            return null;
        }

        return stored.loginId;
    }

    /**
     * 토큰 삭제 (비밀번호 변경 완료 후)
     */
    public void removeToken(String token) {
        tokens.remove(token);
    }

    /**
     * 토큰 저장 클래스
     */
    private static class ResetToken {
        final String loginId;
        final LocalDateTime expiresAt;

        ResetToken(String loginId, LocalDateTime expiresAt) {
            this.loginId = loginId;
            this.expiresAt = expiresAt;
        }
    }
}
