package com.example.personal.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

// 이메일 인증 서비스 (Mock 구현)
// 실제 이메일 발송 없이 콘솔에 인증번호 출력
@Service
public class VerificationService {

    // 이메일별 인증번호 저장 (메모리)
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // 인증번호 유효시간 저장 (5분)
    private final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();

    private static final long EXPIRATION_TIME = 5*60*1000; // 5분

    // 6자리 랜덤 인증번호 생성
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    // 인증번호 발송 (Mock - 콘솔에만 출력)
    public String sendVerificationCode(String email) {
        // 인증번호 생성
        String code = generateCode();

        // 저장
        verificationCodes.put(email, code);
        expirationTimes.put(email, System.currentTimeMillis() + EXPIRATION_TIME);

        // Mock: 콘솔에 출력
        System.out.println("=======================================");
        System.out.println("이메일 인증번호 발송");
        System.out.println("수신자: " + email);
        System.out.println("인증번호: "+ code);
        System.out.println("유효시간: 5분");
        System.out.println("=======================================");

        return code;
    }

    // 인증번호 확인
    public boolean verifyCode(String email, String code) {
        // 저장된 인증번호 확인
        String savedCode = verificationCodes.get(email);
        if (savedCode == null) {
            System.out.println("인증 실패: 발송된 인증번호가 없습니다.");
            return false;
        }

        // 유효시간 확인
        Long expirationTime = expirationTimes.get(email);
        if (expirationTime == null || System.currentTimeMillis() > expirationTime) {
            System.out.println("인증 실패: 인증번호가 만료되었습니다.");
            verificationCodes.remove(email);
            expirationTimes.remove(email);
            return false;
        }

        // 인증번호 일치 확인
        boolean isValid = savedCode.equals(code);

        if (isValid) {
            System.out.println("인증 성공!");
            // 인증 성공 시 삭제 (일회용)
            verificationCodes.remove(email);
            expirationTimes.remove(email);
        } else {
            System.out.println("인증 실패: 인증번호가 일치하지 않습니다.");
        }
        return isValid;
    }
}
