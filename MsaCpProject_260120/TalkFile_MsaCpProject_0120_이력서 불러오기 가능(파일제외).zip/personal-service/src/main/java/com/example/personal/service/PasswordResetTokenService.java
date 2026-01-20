package com.example.personal.service;

// 비밀번호 재설정 토근 관리 서비스
// 토근 유효시간: 10분

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetTokenService {

    // 토큰 -> 아이디 매핑
    private final Map<String, String> tokens =new ConcurrentHashMap<>();

    // 토큰 -> 만료시간 매핑
    private final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();

    private static final long EXPIRATION_TIME = 10 * 60 * 1000;  // 10분

    // 토근 생성
    public String generateToken(String loginId) {
        String token = UUID.randomUUID().toString();

        tokens.put(token, loginId);
        expirationTimes.put(token, System.currentTimeMillis() + EXPIRATION_TIME);

        return token;
    }

    // 토큰 검증
    public boolean validateToken(String token, String loginId) {
        String savedLoginId = tokens.get(token);

        if(savedLoginId == null) {
            System.out.println("토큰 검증 실패: 존재하지 않는 토큰");
            return false;
        }

        if(!savedLoginId.equals(loginId)) {
            System.out.println("토큰 검증 실패: 아이디 불일치");
            return false;
        }

        Long expirationTime = expirationTimes.get(token);
        if(expirationTime == null || System.currentTimeMillis() > expirationTime) {
            System.out.println("토큰 검증 실패: 토큰 만료");
            tokens.remove(token);
            expirationTimes.remove(token);
            return false;
        }

        System.out.println("토큰 검증 성공: " + loginId);
        return true;
    }

    // 토큰 사용 완료 후 삭제
    public void consumeToken(String token) {
        tokens.remove(token);
        expirationTimes.remove(token);
        System.out.println("토큰 사용 완료 및 삭제");
    }
}
