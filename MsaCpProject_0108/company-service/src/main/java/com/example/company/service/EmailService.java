package com.example.company.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 인증번호 이메일 발송
     */
    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[USFK 채용 플랫폼] 이메일 인증번호");
        message.setText(
            "안녕하세요, USFK 채용 플랫폼입니다.\n\n" +
            "요청하신 이메일 인증번호는 아래와 같습니다.\n\n" +
            "인증번호: " + code + "\n\n" +
            "인증번호는 5분간 유효합니다.\n\n" +
            "본 메일은 발신 전용입니다."
        );
        
        mailSender.send(message);
    }
}
