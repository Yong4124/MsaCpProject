package com.example.personal.controller;

import com.example.personal.dto.MemberInfoDto;
import com.example.personal.dto.MemberUpdateDto;
import com.example.personal.dto.PersonalDetailResponse;
import com.example.personal.model.ApprovalYn;
import com.example.personal.model.Personal;
import com.example.personal.repository.PersonalRepository;
import com.example.personal.service.PasswordResetTokenService;
import com.example.personal.service.PersonalService;
import com.example.personal.service.VerificationService;
import com.example.personal.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class PersonalController {

    private final PersonalService personalService;
    private final PersonalRepository personalRepository;
    private final VerificationService verificationService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final JwtUtil jwtUtil;  // ⭐ JWT 추가

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Personal personal) {
        Map<String, Object> response = new HashMap<>();

        try {
            Personal saved = personalService.register(personal);
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("data", saved);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ⭐ JWT 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> loginRequest,
            HttpServletResponse response) {

        Map<String, Object> result = new HashMap<>();

        try {
            String loginId = loginRequest.get("loginId");
            String password = loginRequest.get("password");

            Personal personal = personalService.login(loginId, password);

            // ⭐ JWT 토큰 생성
            String token = jwtUtil.generateToken(personal.getLoginId(), "PERSONAL");

            System.out.println("✅ Personal JWT 토큰 생성!");
            System.out.println("Token: " + token.substring(0, Math.min(50, token.length())) + "...");

            // ⭐ 쿠키에 JWT 저장
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(7200); // 2시간
            response.addCookie(cookie);

            result.put("success", true);
            result.put("message", "로그인 성공");
            result.put("token", token);
            result.put("data", personal);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    // ID 중복 체크
    @GetMapping("/check-id/{loginId}")
    public ResponseEntity<Map<String, Object>> checkId(@PathVariable String loginId) {
        Map<String, Object> response = new HashMap<>();

        boolean exists = personalService.checkIdDuplication(loginId);
        response.put("exists", exists);
        response.put("message", exists ? "이미 사용중인 ID입니다." : "사용 가능한 ID입니다.");

        return ResponseEntity.ok(response);
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

    // 이메일 중복 체크
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmail(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();

        boolean exists = personalService.checkEmailDuplication(email);
        response.put("exists", exists);
        response.put("message", exists ? "이미 사용중인 이메일입니다." : "사용 가능한 이메일입니다.");

        return ResponseEntity.ok(response);
    }

    // 회원 조회
    @GetMapping("/{loginId}")
    public ResponseEntity<Personal> getPersonal(@PathVariable String loginId) {
        return personalService.findByLoginId(loginId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 전체 회원 조회
    @GetMapping("/list")
    public ResponseEntity<Iterable<Personal>> getAllPersonals() {
        return ResponseEntity.ok(personalService.findAll());
    }

    // 이메일 인증번호 발송
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, Object>> sendVerification(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");

            // 이메일 입력 확인
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이메일 중복 체크
            if (personalService.checkEmailDuplication(email)) {
                response.put("success", false);
                response.put("message", "이미 사용중인 이메일입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 인증번호 발송
            String code = verificationService.sendVerificationCode(email);

            response.put("success", true);
            response.put("message", "인증번호가 발송되었습니다. 콘솔을 확인하세요.");
            response.put("code", code);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증번호 발송 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 이메일 인증번호 확인
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String code = request.get("code");

            // 이메일 입력 확인
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 인증번호 입력 확인
            if (code == null || code.isEmpty()) {
                response.put("success", false);
                response.put("message", "인증번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 인증번호 확인
            boolean isValid = verificationService.verifyCode(email, code);

            if (isValid) {
                response.put("success", true);
                response.put("message", "인증이 완료되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "인증번호가 일치하지 않거나 만료되었습니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증 확인 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<Map<String, Object>> findId(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String name = request.get("name");
            String email = request.get("email");

            // 입력 확인
            if (name == null || name.isEmpty()) {
                response.put("success", false);
                response.put("message", "이름을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 아이디 찾기
            Optional<String> foundId = personalService.findIdByNameAndEmail(name, email);

            if (foundId.isPresent()) {
                response.put("success", true);
                response.put("message", "아이디를 찾았습니다.");
                response.put("loginId", foundId.get());
            } else {
                response.put("success", false);
                response.put("message", "일치하는 회원 정보가 없습니다.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "아이디 찾기 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 비밀번호 재설정 - 인증번호 발송
    @PostMapping("/send-password-reset")
    public ResponseEntity<Map<String, Object>> sendPasswordReset(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String loginId = request.get("loginId");
            String name = request.get("name");
            String email = request.get("email");

            // 입력 확인
            if (loginId == null || loginId.isEmpty()) {
                response.put("success", false);
                response.put("message", "아이디를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (name == null || name.isEmpty()) {
                response.put("success", false);
                response.put("message", "이름을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 회원 정보 확인
            boolean isValid = personalService.verifyUser(loginId, name, email);

            if (!isValid) {
                response.put("success", false);
                response.put("message", "회원 정보가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 인증번호 발송
            String code = verificationService.sendVerificationCode(email);

            response.put("success", true);
            response.put("message", "인증번호가 발송되었습니다.");
            response.put("code", code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증번호 발송 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 비밀번호 재설정 - 인증번호 확인 및 토큰 발급
    @PostMapping("/verify-password-reset")
    public ResponseEntity<Map<String, Object>> verifyPasswordReset(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String loginId = request.get("loginId");
            String email = request.get("email");
            String code = request.get("code");

            // 입력 확인
            if (loginId == null || loginId.isEmpty()) {
                response.put("success", false);
                response.put("message", "아이디를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (code == null || code.isEmpty()) {
                response.put("success", false);
                response.put("message", "인증번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 인증번호 확인
            boolean isVerified = verificationService.verifyCode(email, code);

            if (!isVerified) {
                response.put("success", false);
                response.put("message", "인증번호가 일치하지 않거나 만료되었습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 토큰 생성
            String token = passwordResetTokenService.generateToken(loginId);

            response.put("success", true);
            response.put("message", "인증이 완료되었습니다.");
            response.put("token", token);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증 확인 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 비밀번호 재설정 - 새 비밀번호 저장
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String loginId = request.get("loginId");
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            // 입력 확인
            if (loginId == null || loginId.isEmpty()) {
                response.put("success", false);
                response.put("message", "아이디를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (token == null || token.isEmpty()) {
                response.put("success", false);
                response.put("message", "토큰이 유효하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword == null || newPassword.isEmpty()) {
                response.put("success", false);
                response.put("message", "새 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 토큰 검증
            boolean isValidToken = passwordResetTokenService.validateToken(token, loginId);

            if (!isValidToken) {
                response.put("success", false);
                response.put("message", "토큰이 만료되었거나 유효하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 비밀번호 재설정
            personalService.resetPassword(loginId, newPassword);

            // 토큰 사용 완료
            passwordResetTokenService.consumeToken(token);

            response.put("success", true);
            response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ⭐ JWT 내 정보 조회
    @GetMapping("/my-info")
    public ResponseEntity<Map<String, Object>> getMyInfo(
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        Map<String, Object> response = new HashMap<>();

        try {
            // ⭐ JWT 검증
            if (token == null || !jwtUtil.isTokenValid(token)) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String loginId = jwtUtil.extractLoginId(token);

            Personal personal = personalService.getMemberInfo(loginId);
            MemberInfoDto memberInfo = MemberInfoDto.fromEntity(personal);

            response.put("success", true);
            response.put("member", memberInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원정보 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ⭐ JWT 회원정보 수정
    @PutMapping("/update-info")
    public ResponseEntity<Map<String, Object>> updateInfo(
            @RequestBody MemberUpdateDto updateDto,
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        Map<String, Object> response = new HashMap<>();

        try {
            // ⭐ JWT 검증
            if (token == null || !jwtUtil.isTokenValid(token)) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String loginId = jwtUtil.extractLoginId(token);

            personalService.updateMemberInfo(loginId, updateDto);

            response.put("success", true);
            response.put("message", "회원정보가 성공적으로 수정되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원정보 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 이메일 인증번호 발송 (마이페이지 - 이메일 변경용)
    @PostMapping("/send-email-verification")
    public ResponseEntity<Map<String, Object>> sendEmailVerification(
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");

            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 인증번호 생성 및 발송
            String code = verificationService.sendVerificationCode(email);

            response.put("success", true);
            response.put("message", "인증번호가 발송되었습니다.");
            response.put("code", code);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증번호 발송 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ⭐ JWT 회원 탈퇴
    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @RequestBody Map<String, String> request,
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            HttpServletResponse response) {

        Map<String, Object> result = new HashMap<>();

        try {
            // ⭐ JWT 검증
            if (token == null || !jwtUtil.isTokenValid(token)) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            String loginId = jwtUtil.extractLoginId(token);

            String password = request.get("password");

            if (password == null || password.isEmpty()) {
                result.put("success", false);
                result.put("message", "비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(result);
            }

            personalService.deleteMember(loginId, password);

            // ⭐ JWT 쿠키 삭제
            Cookie cookie = new Cookie("JWT_TOKEN", null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            result.put("success", true);
            result.put("message", "회원 탈퇴가 완료되었습니다.");
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "회원 탈퇴 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // ⭐ JWT 로그인 체크
    @GetMapping("/check-login")
    public ResponseEntity<Map<String, Object>> checkLogin(
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || !jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401)
                    .body(Map.of("isLoggedIn", false));
        }

        return ResponseEntity.ok(Map.of("isLoggedIn", true));
    }

    // ⭐ JWT 세션 체크 (마이페이지 분기용)
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkSession(
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        System.out.println("===== Personal check-session (JWT) =====");
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

    // admin - 회원 목록
    @GetMapping("/admin/users")
    public ResponseEntity<List<Map<String, Object>>> adminUsers() {

        List<Map<String, Object>> result = personalService.adminList().stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("no", p.getSeqNoM100());
                    m.put("name", p.getName());
                    m.put("id", p.getLoginId());
                    m.put("gender", p.getGender());
                    m.put("residence", p.getResidence());
                    m.put("date", p.getInsertDate());
                    m.put("approve", p.getApprovalYn());
                    return m;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // admin - 회원 상세
    @GetMapping("/admin/users/{seq}")
    public ResponseEntity<PersonalDetailResponse> detail(@PathVariable Integer seq) {

        Personal p = personalRepository.findById(seq)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(PersonalDetailResponse.from(p));
    }

    // admin - 승인 상태 변경
    @PutMapping("/admin/users/{seq}/approval")
    public ResponseEntity<Void> updateApproval(
            @PathVariable Integer seq,
            @RequestBody Map<String, String> body
    ) {
        String approvalYn = body.get("approvalYn");
        if (!"Y".equalsIgnoreCase(approvalYn) && !"N".equalsIgnoreCase(approvalYn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "approvalYn must be Y or N");
        }

        Personal p = personalRepository.findById(seq)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        p.setApprovalYn("Y".equalsIgnoreCase(approvalYn) ? ApprovalYn.Y : ApprovalYn.N);
        personalRepository.save(p);

        return ResponseEntity.ok().build();
    }

    // admin - 통계
    @GetMapping("/admin/stats/join-monthly")
    public ResponseEntity<Map<String, Object>> joinMonthly(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(personalService.getJoinStats(from, to));
    }
}