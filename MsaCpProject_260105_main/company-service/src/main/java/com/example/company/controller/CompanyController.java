package com.example.company.controller;

import com.example.company.dto.CompanyLoginRequest;
import com.example.company.dto.CompanyRegisterRequest;
import com.example.company.dto.EmailVerificationRequest;
import com.example.company.service.CompanyService;
import jakarta.servlet.http.HttpSession;
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

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody CompanyRegisterRequest request) {
        return ResponseEntity.ok(companyService.register(request));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody CompanyLoginRequest request, HttpSession session) {
        Map<String, Object> result = companyService.login(request);
        if ((boolean) result.get("success")) {
            session.setAttribute("companyUser", result.get("data"));
            session.setAttribute("companyUserId", request.getLoginId());
        }
        return ResponseEntity.ok(result);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
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

    // 내 정보 조회
    @GetMapping("/myinfo")
    public ResponseEntity<Map<String, Object>> getMyInfo(HttpSession session) {
        String loginId = (String) session.getAttribute("companyUserId");
        if (loginId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.ok(companyService.getMemberInfo(loginId));
    }

    // 회원정보 수정
    @PutMapping("/update-info")
    public ResponseEntity<Map<String, Object>> updateInfo(@RequestBody Map<String, Object> request, HttpSession session) {
        String loginId = (String) session.getAttribute("companyUserId");
        if (loginId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.ok(companyService.updateMemberInfo(loginId, request));
    }

    // 회원 탈퇴
    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestBody Map<String, String> request, HttpSession session) {
        String loginId = (String) session.getAttribute("companyUserId");
        if (loginId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(result);
        }

        String password = request.get("password");
        Map<String, Object> result = companyService.deleteAccount(loginId, password);

        if ((boolean) result.get("success")) {
            session.invalidate();
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

    // 세션 체크
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String loginId = (String) session.getAttribute("companyUserId");
        result.put("loggedIn", loginId != null);
        if (loginId != null) {
            result.put("loginId", loginId);
        }
        return ResponseEntity.ok(result);
    }
}
