package com.example.personal.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncoder {

    // 메서드 1: SHA-512 비밀번호 암호화
    public static String encode(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // salt를 비밀번호에 추가
            String saltedPassword = password + salt;

            // SHA-512 해시 생성
            byte[] hash = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));

            // Base64로 인코딩하여 문자열로 변환
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 암호화 실패",e);
        }
    }

    // 메서드 2: Salt 생성
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // 메서드 3: 비밀번호 검증
    public static boolean matches(String rawPassword, String encodedPassword, String salt) {
        String hashedPassword = encode(rawPassword, salt);
        return hashedPassword.equals(encodedPassword);
    }
}