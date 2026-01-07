package com.example.company.util;

import com.example.company.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final EmailService emailService;
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    /**
     * 인증번호 생성 및 저장 + 실제 이메일 발송
     */
    public String generateAndSaveCode(String email) {
        String code = generateCode();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        verificationCodes.put(email, new VerificationCode(code, expiryTime));

        // 콘솔에 출력 (개발용)
        System.out.println("========================================");
        System.out.println("[이메일 인증] 수신자: " + email);
        System.out.println("[이메일 인증] 인증번호: " + code);
        System.out.println("[이메일 인증] 만료시간: " + expiryTime);
        System.out.println("========================================");

        // 실제 이메일 발송
        try {
            emailService.sendVerificationEmail(email, code);
            System.out.println("[이메일 인증] 이메일 발송 성공!");
        } catch (Exception e) {
            System.out.println("[이메일 인증] 이메일 발송 실패: " + e.getMessage());
            // 이메일 발송 실패해도 인증번호는 반환 (개발 편의)
        }

        return code;
    }

    /**
     * 인증번호 검증
     */
    public boolean verifyCode(String email, String code) {
        VerificationCode stored = verificationCodes.get(email);

        if (stored == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(stored.expiryTime)) {
            verificationCodes.remove(email);
            return false;
        }

        if (stored.code.equals(code)) {
            verificationCodes.remove(email);
            return true;
        }

        return false;
    }

    /**
     * 6자리 인증번호 생성
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private static class VerificationCode {
        String code;
        LocalDateTime expiryTime;

        VerificationCode(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}