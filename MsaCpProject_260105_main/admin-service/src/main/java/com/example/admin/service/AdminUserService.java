package com.example.admin.service;

import com.example.admin.model.AdminRole;
import com.example.admin.model.AdminUser;
import com.example.admin.repository.AdminUserRepository;
import com.example.admin.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Pattern;

@Service // 스프링이 이 클래스를 Service Bean으로 등록
public class AdminUserService {

    // DB 접근용 Repository (JPA)
    private final AdminUserRepository repository;

    // ✅ 비밀번호 정책(정규식)
    // - 8~16자
    // - 영문 1개 이상
    // - 숫자 1개 이상
    // - 특수문자 1개 이상(아래 지정된 문자들)
    //
    // 정규식 구성:
    // ^                : 문자열 시작
    // (?=.*[A-Za-z])   : 영문 최소 1개 포함
    // (?=.*\\d)        : 숫자 최소 1개 포함
    // (?=.*[...])      : 특수문자 최소 1개 포함
    // .{8,16}          : 전체 길이 8~16
    // $                : 문자열 끝
    private static final Pattern PASSWORD_POLICY = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=\\[\\]{};:'\",.<>/?]).{8,16}$"
    );

    // 생성자 주입(권장)
    public AdminUserService(AdminUserRepository repository) {
        this.repository = repository;
    }

    /**
     * 관리자 로그인
     *
     * @param loginId     로그인 아이디
     * @param rawPassword 사용자가 입력한 "원문" 비밀번호
     * @return LoginResult(토큰, 이름, 권한명, 권한코드)
     */
    public LoginResult login(String loginId, String rawPassword) {

        // 1) 아이디로 사용자 조회 (삭제되지 않은 사용자만 delYn='N')
        //    없으면 예외 -> "아이디 또는 비밀번호가 올바르지 않습니다." (보안상 동일 메시지)
        AdminUser user = repository.findByLoginIdAndDelYn(loginId, "N")
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 2) 입력된 비밀번호(raw)를 SHA-512 해시로 변환
        //    DB에는 해시값이 저장되어 있으므로 동일하게 해싱 후 비교해야 함
        String hashed = PasswordUtil.sha512(rawPassword);

        // 3) 해시값 비교 (틀리면 동일 에러 메시지)
        if (!hashed.equals(user.getPw())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 4) 토큰 발급(샘플)
        //    운영에서는 보통 JWT + 만료시간 + 서명 등을 사용
        String token = UUID.randomUUID().toString();

        // 5) 로그인 성공 결과 반환
        //    - user.getRole().getLabel() : 화면 표시용(예: "슈퍼관리자")
        //    - user.getRole().getCode()  : 시스템 코드용(예: "SUPER_ADMIN")
        return new LoginResult(
                token,
                user.getNm(),
                user.getRole().getLabel(),
                user.getRole().getCode()
        );
    }

    /**
     * 기본 슈퍼관리자(admin) 계정이 없으면 1회 생성
     * 보통 서버 시작 시 호출되도록 구성(@PostConstruct, Runner 등)
     */
    public void ensureDefaultSuperAdmin() {
        System.out.println("### ensureDefaultSuperAdmin called");

        // 이미 admin 계정이 존재하면 생성하지 않고 종료
        if (repository.existsByLoginIdAndDelYn("admin", "N")) return;

        // 새 관리자 생성
        AdminUser admin = new AdminUser();
        admin.setLoginId("admin");

        // 기본 비밀번호(예시)
        // ⚠ 운영에서는 환경변수/초기화 과정에서 변경 유도 등이 필요
        String defaultPw = "admin123!";

        // 기본 비밀번호가 정책을 만족하는지 검사
        if (!PASSWORD_POLICY.matcher(defaultPw).matches()) {
            throw new IllegalStateException("기본 관리자 비밀번호가 정책을 만족하지 않습니다.");
        }

        // DB 저장은 "해시값"으로 저장 (원문 저장 금지)
        admin.setPw(PasswordUtil.sha512(defaultPw));

        // 나머지 기본 정보 세팅
        admin.setNm("슈퍼관리자");
        admin.setEmail("admin@local");
        admin.setDepartment("관리자");
        admin.setPhone("010-0000-0000");
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setInsertDate(LocalDate.now());
        admin.setDelYn("N");

        // DB에 저장
        repository.save(admin);
    }

    /**
     * (선택) 관리자 생성/수정에서 공통으로 쓸 수 있는 비밀번호 처리 유틸
     * - null/blank 체크
     * - 정책 검사
     * - 통과하면 SHA-512 해시 반환
     */
    public String encodePasswordWithPolicy(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력하세요.");
        }
        if (!PASSWORD_POLICY.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("비밀번호는 8~16자의 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
        return PasswordUtil.sha512(rawPassword);
    }

    /**
     * 로그인 응답 DTO (Java record)
     * - 불변(immutable) 데이터 객체
     * - getter 자동 생성
     */
    public record LoginResult(String token, String name, String roleName, String roleCode) {}
}
