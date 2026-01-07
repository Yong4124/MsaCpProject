package com.example.admin.security;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class AdminTokenStore {

    // token -> TokenInfo
    private final Map<String, TokenInfo> store = new ConcurrentHashMap<>();

    /**
     * ✅ roleCode 정규화
     * - "ROLE_SUPER_ADMIN" -> "SUPER_ADMIN"
     * - "1" -> "SUPER_ADMIN"  (요구사항: SUPER_ADMIN("1")일 때만)
     */
    private static String normalizeRoleCode(String roleCode) {
        if (roleCode == null) return "";
        String r = roleCode.trim();

        // ROLE_ prefix 제거
        r = r.replaceFirst("^ROLE_", "");

        // ✅ 요구사항: SUPER_ADMIN이 "1"인 경우만 매핑
        if ("1".equals(r)) return "SUPER_ADMIN";

        return r;
    }

    public void save(String token, String loginId, String roleCode) {
        // ✅ 저장 시점에 정규화해서 넣어두면, 이후 권한 체크가 안정적
        String normalized = normalizeRoleCode(roleCode);
        store.put(token, new TokenInfo(token, loginId, normalized));
    }

    public Optional<TokenInfo> find(String token) {
        return Optional.ofNullable(store.get(token));
    }

    public void delete(String token) {
        store.remove(token);
    }

    public record TokenInfo(String token, String loginId, String roleCode) {

        public SimpleGrantedAuthority toAuthority() {
            // ✅ 여기서도 한번 더 안전하게 정규화
            String normalized = normalizeRoleCode(roleCode);
            return new SimpleGrantedAuthority("ROLE_" + normalized);
        }

        private static String normalizeRoleCode(String roleCode) {
            if (roleCode == null) return "";
            String r = roleCode.trim();
            r = r.replaceFirst("^ROLE_", "");
            if ("1".equals(r)) return "SUPER_ADMIN";
            return r;
        }
    }
}
