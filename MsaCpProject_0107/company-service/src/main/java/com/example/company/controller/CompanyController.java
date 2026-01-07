package com.example.company.controller;

import com.example.company.dto.CompanyLoginRequest;
import com.example.company.dto.CompanyRegisterRequest;
import com.example.company.dto.EmailVerificationRequest;
import com.example.company.service.CompanyService;
import com.example.company.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final JwtUtil jwtUtil;  // ⭐ JWT 추가

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody CompanyRegisterRequest request) {
        return ResponseEntity.ok(companyService.register(request));
    }

    // ⭐ JWT 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody CompanyLoginRequest request,
            HttpServletResponse response) {

        System.out.println("===== 기업 로그인 시도 (JWT) =====");
        System.out.println("loginId: " + request.getLoginId());

        Map<String, Object> result = companyService.login(request);

        System.out.println("로그인 결과 success: " + result.get("success"));
        System.out.println("로그인 결과 message: " + result.get("message"));

        if ((boolean) result.get("success")) {
            // ⭐ JWT 토큰 생성
            String token = jwtUtil.generateToken(request.getLoginId(), "COMPANY");

            System.out.println("✅ Company JWT 토큰 생성!");
            System.out.println("Token: " + token.substring(0, Math.min(50, token.length())) + "...");

            // ⭐ 쿠키에 JWT 저장
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(86400); // 24시간
            response.addCookie(cookie);

            result.put("token", token);
        } else {
            System.out.println("❌ 로그인 실패");
        }

        return ResponseEntity.ok(result);
    }

    // ⭐ JWT 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        // JWT 쿠키 삭제
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "로그아웃 되었습니다.");
        return ResponseEntity.ok(result);
    }

    // 아이디 중복 확인
    @GetMapping("/check-id/{loginId}")
    public ResponseEntity<Map<String, Object>> checkId(@PathVariable String loginId) {
        return ResponseEntity.ok(companyService.checkIdExists(loginId));
    }

    // 이메일 인증번호 발송
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, Object>> sendVerification(@RequestBody EmailVerificationRequest request) {
        return ResponseEntity.ok(companyService.sendVerificationEmail(request.getEmail()));
    }

    // ⭐ JWT 내 정보 조회
    @GetMapping("/myinfo")
    public ResponseEntity<Map<String, Object>> getMyInfo(
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || !jwtUtil.isTokenValid(token)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(result);
        }

        String loginId = jwtUtil.extractLoginId(token);
        return ResponseEntity.ok(companyService.getMemberInfo(loginId));
    }

    // ⭐ JWT 회원정보 수정
    @PutMapping("/update-info")
    public ResponseEntity<Map<String, Object>> updateInfo(
            @RequestBody Map<String, Object> request,
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || !jwtUtil.isTokenValid(token)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(result);
        }

        String loginId = jwtUtil.extractLoginId(token);
        return ResponseEntity.ok(companyService.updateMemberInfo(loginId, request));
    }

    // ⭐ JWT 회원 탈퇴
    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @RequestBody Map<String, String> request,
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            HttpServletResponse response) {

        if (token == null || !jwtUtil.isTokenValid(token)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(result);
        }

        String loginId = jwtUtil.extractLoginId(token);
        String password = request.get("password");
        Map<String, Object> result = companyService.deleteAccount(loginId, password);

        if ((boolean) result.get("success")) {
            // ⭐ JWT 쿠키 삭제
            Cookie cookie = new Cookie("JWT_TOKEN", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(result);
    }

    // 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<Map<String, Object>> findId(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String managerNm = request.get("managerNm");
        return ResponseEntity.ok(companyService.findId(email, managerNm));
    }

    // 비밀번호 재설정 요청
    @PostMapping("/reset-password-request")
    public ResponseEntity<Map<String, Object>> resetPasswordRequest(@RequestBody Map<String, String> request) {
        String loginId = request.get("loginId");
        String email = request.get("email");
        return ResponseEntity.ok(companyService.resetPasswordRequest(loginId, email));
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        return ResponseEntity.ok(companyService.resetPassword(token, newPassword));
    }

    // ⭐ JWT 세션 체크 (마이페이지 분기용)
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkSession(
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        System.out.println("===== Company check-session (JWT) =====");
        System.out.println("Token: " + (token != null ? token.substring(0, Math.min(50, token.length())) + "..." : "null"));

        Map<String, Object> result = new HashMap<>();

        if (token != null && jwtUtil.isTokenValid(token)) {
            String loginId = jwtUtil.extractLoginId(token);
            String memberType = jwtUtil.extractMemberType(token);

            System.out.println("✅ 유효한 JWT!");
            System.out.println("loginId: " + loginId);
            System.out.println("memberType: " + memberType);

            result.put("loggedIn", true);
            result.put("loginId", loginId);
            result.put("memberType", memberType);
        } else {
            System.out.println("❌ JWT 없음 또는 만료");
            result.put("loggedIn", false);
        }

        return ResponseEntity.ok(result);
    }
}