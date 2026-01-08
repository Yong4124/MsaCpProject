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
    private final Map<String, LocalDateTime> verifiedEmails = new ConcurrentHashMap<>();  // 인증 완료된 이메일 저장

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
     * 인증번호 검증 (확인 버튼용 - 인증 상태 저장)
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
            // 인증 완료 상태 저장 (10분간 유효)
            verifiedEmails.put(email, LocalDateTime.now().plusMinutes(10));
            return true;
        }

        return false;
    }

    /**
     * 인증번호 검증만 수행 (삭제하지 않음 - 수정 시 사용)
     */
    public boolean verifyCodeOnly(String email, String code) {
        VerificationCode stored = verificationCodes.get(email);

        if (stored == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(stored.expiryTime)) {
            verificationCodes.remove(email);
            return false;
        }

        return stored.code.equals(code);
    }

    /**
     * 인증번호 삭제
     */
    public void removeCode(String email) {
        verificationCodes.remove(email);
    }

    /**
     * 이메일 인증 완료 상태 확인 (수정 시 사용)
     */
    public boolean isEmailVerified(String email) {
        LocalDateTime expiryTime = verifiedEmails.get(email);
        
        if (expiryTime == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(expiryTime)) {
            verifiedEmails.remove(email);
            return false;
        }
        
        return true;
    }

    /**
     * 인증 완료 상태 제거 (수정 완료 후 호출)
     */
    public void clearVerifiedEmail(String email) {
        verifiedEmails.remove(email);
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