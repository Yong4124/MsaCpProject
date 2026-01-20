package com.example.admin.service;

import com.example.admin.model.AdminRole;
import com.example.admin.model.AdminUser;
import com.example.admin.repository.AdminUserRepository;
import com.example.admin.security.AdminTokenStore;   // ✅ 추가
import com.example.admin.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AdminUserService {

    private final AdminUserRepository repository;
    private final AdminTokenStore tokenStore; // ✅ 추가

    private static final Pattern PASSWORD_POLICY = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=\\[\\]{};:'\",.<>/?]).{8,16}$"
    );

    // ✅ 생성자에 tokenStore 추가
    public AdminUserService(AdminUserRepository repository, AdminTokenStore tokenStore) {
        this.repository = repository;
        this.tokenStore = tokenStore;
    }

    /**
     * 관리자 로그인
     */
    public LoginResult login(String loginId, String rawPassword) {

        AdminUser user = repository.findByLoginIdAndDelYn(loginId, "N")
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        String hashed = PasswordUtil.sha512(rawPassword);

        if (!hashed.equals(user.getPw())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // ✅ 토큰 발급
        String token = UUID.randomUUID().toString();

        // ✅ [B 방식 핵심] 토큰 저장 (필터가 이걸로 사용자/권한을 찾음)
        // user.getRole().getCode() 예: "SUPER_ADMIN", "KCCI_ADMIN", "KUSAF_ADMIN"
        tokenStore.save(token, user.getLoginId(), user.getRole().getCode());

        return new LoginResult(
                token,
                user.getNm(),
                user.getRole().getLabel(),
                user.getRole().getCode()
        );
    }

    public void ensureDefaultSuperAdmin() {
        System.out.println("### ensureDefaultSuperAdmin called");

        if (repository.existsByLoginIdAndDelYn("admin", "N")) return;

        AdminUser admin = new AdminUser();
        admin.setLoginId("admin");

        String defaultPw = "admin123!";

        if (!PASSWORD_POLICY.matcher(defaultPw).matches()) {
            throw new IllegalStateException("기본 관리자 비밀번호가 정책을 만족하지 않습니다.");
        }

        admin.setPw(PasswordUtil.sha512(defaultPw));
        admin.setNm("슈퍼관리자");
        admin.setEmail("admin@local");
        admin.setDepartment("관리자");
        admin.setPhone("010-0000-0000");
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setInsertDate(LocalDate.now());
        admin.setDelYn("N");

        repository.save(admin);
    }

    public String encodePasswordWithPolicy(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력하세요.");
        }
        if (!PASSWORD_POLICY.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("비밀번호는 8~16자의 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
        return PasswordUtil.sha512(rawPassword);
    }

    public void applyPasswordIfPresent(AdminUser user, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) return;
        user.setPw(encodePasswordWithPolicy(rawPassword));
    }

    public record LoginResult(String token, String name, String roleName, String roleCode) {}
}
