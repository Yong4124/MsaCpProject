package com.example.admin.controller;

import com.example.admin.model.AdminRole;
import com.example.admin.model.AdminUser;
import com.example.admin.repository.AdminUserRepository;
import com.example.admin.service.AdminUserService;
import com.example.admin.util.PasswordUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminUserService adminUserService;
    private final AdminUserRepository adminUserRepository;

    // ✅ 비밀번호 정책: 8~16자, 영문+숫자+특수문자 포함
    private static final Pattern PASSWORD_POLICY = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=\\[\\]{};:'\",.<>/?]).{8,16}$"
    );

    public AdminAuthController(AdminUserService adminUserService,
                               AdminUserRepository adminUserRepository) {
        this.adminUserService = adminUserService;
        this.adminUserRepository = adminUserRepository;
    }

    // =========================
    // 1) 로그인
    // POST /api/admin/login
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            var result = adminUserService.login(req.id(), req.password()); // id = loginId로 사용
            return ResponseEntity.ok(new LoginResponse(
                    result.token(),
                    result.name(),
                    result.roleCode(),
                    result.roleName()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================
    // 2) 관리자 목록 + 검색(통합)
    // GET /api/admin/users
    //  - 전체: /api/admin/users
    //  - 검색: /api/admin/users?field=id&keyword=admin
    // =========================
    @GetMapping("/users")
    public List<AdminUserResponse> listAdmins(
            @RequestParam(value = "field", required = false, defaultValue = "all") String field,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword
    ) {
        var list = (keyword == null || keyword.isBlank())
                ? adminUserRepository.findAllByDelYnOrderBySeqNoC010Desc("N")
                : adminUserRepository.searchAdmins(field, keyword.trim());

        return list.stream().map(AdminUserResponse::from).toList();
    }

    // =========================
    // 3) 아이디 중복확인
    // GET /api/admin/users/check?id=xxx
    // =========================
    @GetMapping("/users/check")
    public ResponseEntity<?> checkDuplicate(@RequestParam("id") String id) {
        boolean exists = adminUserRepository.existsByLoginIdAndDelYn(id, "N");
        return ResponseEntity.ok(new CheckResponse(!exists, exists ? "이미 사용 중" : "사용 가능"));
    }

    // =========================
    // 4) 관리자 등록 (SUPER_ADMIN만)
    // POST /api/admin/users
    // =========================
    @PostMapping("/users")
    public ResponseEntity<?> createAdmin(@RequestBody CreateAdminRequest req,
                                         Authentication authentication) {
        // ✅ SUPER_ADMIN만 허용
        var deny = requireSuper(authentication);
        if (deny != null) return deny;

        try {
            if (req.id() == null || req.id().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("아이디는 필수입니다."));
            }
            if (req.password() == null || req.password().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("비밀번호는 필수입니다."));
            }
            if (req.name() == null || req.name().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("성명은 필수입니다."));
            }
            if (req.email() == null || req.email().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("이메일은 필수입니다."));
            }
            if (req.role() == null || req.role().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("권한은 필수입니다."));
            }

            // ✅ 비밀번호 정책 검사 (백엔드에서도 꼭)
            if (!PASSWORD_POLICY.matcher(req.password()).matches()) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse("비밀번호는 8~16자의 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
                );
            }

            boolean exists = adminUserRepository.existsByLoginIdAndDelYn(req.id(), "N");
            if (exists) {
                return ResponseEntity.badRequest().body(new ErrorResponse("이미 사용 중인 아이디입니다."));
            }

            AdminUser u = new AdminUser();
            u.setLoginId(req.id());

            // ✅ SHA-512로 해시 저장 (128자리 hex)
            u.setPw(PasswordUtil.sha512(req.password()));

            u.setNm(req.name());
            u.setEmail(req.email());
            u.setDepartment(req.department());
            u.setPhone(req.phone()); // phone nullable=false면 필수
            u.setRole(mapRole(req.role())); // SUPER/COMPANY/PERSONAL -> enum
            u.setInsertDate(LocalDate.now());
            u.setDelYn("N");

            adminUserRepository.save(u);

            return ResponseEntity.ok(new SimpleResponse("OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================
    // 5) 관리자 상세조회
    // GET /api/admin/users/{loginId}
    // =========================
    @GetMapping("/users/{loginId}")
    public ResponseEntity<?> detail(@PathVariable String loginId) {
        try {
            var user = adminUserRepository.findByLoginIdAndDelYn(loginId, "N")
                    .orElseThrow(() -> new IllegalArgumentException("해당 관리자를 찾을 수 없습니다."));
            return ResponseEntity.ok(AdminDetailResponse.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================
    // 6) 관리자 수정 (SUPER_ADMIN만)
    // PUT /api/admin/users/{loginId}
    // =========================
    @PutMapping("/users/{loginId}")
    public ResponseEntity<?> update(@PathVariable String loginId,
                                    @RequestBody UpdateAdminRequest req,
                                    Authentication authentication) {
        // ✅ SUPER_ADMIN만 허용
        var deny = requireSuper(authentication);
        if (deny != null) return deny;

        try {
            var user = adminUserRepository.findByLoginIdAndDelYn(loginId, "N")
                    .orElseThrow(() -> new IllegalArgumentException("해당 관리자를 찾을 수 없습니다."));

            // ✅ 비밀번호 변경(입력했을 때만) + 정책 검사
            if (req.password() != null && !req.password().isBlank()) {
                if (!PASSWORD_POLICY.matcher(req.password()).matches()) {
                    return ResponseEntity.badRequest().body(
                            new ErrorResponse("비밀번호는 8~16자의 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
                    );
                }
            }
            // 서비스 위임(실제 저장/적용)
            adminUserService.applyPasswordIfPresent(user, req.password());

            if (req.name() != null) user.setNm(req.name());
            if (req.email() != null) user.setEmail(req.email());
            if (req.department() != null) user.setDepartment(req.department());
            if (req.phone() != null) user.setPhone(req.phone());
            if (req.role() != null && !req.role().isBlank()) user.setRole(mapRole(req.role()));

            adminUserRepository.save(user);
            return ResponseEntity.ok(new SimpleResponse("OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================
    // 7) 관리자 삭제(소프트 삭제) (SUPER_ADMIN만)
    // DELETE /api/admin/users/{loginId}
    // =========================
    @DeleteMapping("/users/{loginId}")
    public ResponseEntity<?> delete(@PathVariable String loginId,
                                    Authentication authentication) {
        // ✅ SUPER_ADMIN만 허용
        var deny = requireSuper(authentication);
        if (deny != null) return deny;

        try {
            var user = adminUserRepository.findByLoginIdAndDelYn(loginId, "N")
                    .orElseThrow(() -> new IllegalArgumentException("해당 관리자를 찾을 수 없습니다."));

            user.setDelYn("Y");
            adminUserRepository.save(user);
            return ResponseEntity.ok(new SimpleResponse("OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // =========================
    // ✅ 권한 체크 유틸 (SUPER_ADMIN만)
    // =========================
    private ResponseEntity<?> requireSuper(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("인증이 필요합니다."));
        }

        boolean isSuper = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));

        if (!isSuper) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("슈퍼관리자만 가능합니다."));
        }
        return null; // 통과
    }

    // =========================
    // 내부 유틸: role 매핑
    // =========================
    private AdminRole mapRole(String role) {
        return switch (role) {
            case "SUPER" -> AdminRole.SUPER_ADMIN;
            case "COMPANY" -> AdminRole.KCCI_ADMIN;
            case "PERSONAL" -> AdminRole.KUSAF_ADMIN;
            default -> throw new IllegalArgumentException("권한 값이 올바르지 않습니다.");
        };
    }

    // =========================
    // DTOs
    // =========================
    public record LoginRequest(String id, String password) {}
    public record LoginResponse(String token, String name, String role, String roleName) {}
    public record ErrorResponse(String message) {}

    public record CreateAdminRequest(
            String id,
            String password,
            String name,
            String email,
            String department,
            String phone,
            String role
    ) {}

    public record UpdateAdminRequest(
            String password, String name, String email, String department, String phone, String role
    ) {}

    public record CheckResponse(boolean available, String message) {}
    public record SimpleResponse(String result) {}

    // 목록 응답
    public record AdminUserResponse(Long no, String id, String name, String roleName) {
        static AdminUserResponse from(AdminUser u) {
            return new AdminUserResponse(
                    u.getSeqNoC010(),
                    u.getLoginId(),
                    u.getNm(),
                    u.getRole().getLabel()
            );
        }
    }

    // 상세 응답
    public record AdminDetailResponse(
            String id, String name, String email, String department, String phone, String role
    ) {
        static AdminDetailResponse from(AdminUser u) {
            return new AdminDetailResponse(
                    u.getLoginId(),
                    u.getNm(),
                    u.getEmail(),
                    u.getDepartment(),
                    u.getPhone(),
                    switch (u.getRole()) {
                        case SUPER_ADMIN -> "SUPER";
                        case KCCI_ADMIN -> "COMPANY";
                        case KUSAF_ADMIN -> "PERSONAL";
                    }
            );
        }
    }
}
