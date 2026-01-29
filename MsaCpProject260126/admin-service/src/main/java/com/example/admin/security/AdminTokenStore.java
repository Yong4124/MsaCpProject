package com.example.admin.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AdminTokenStore {

    // token -> TokenInfo
    private final Map<String, TokenInfo> store = new ConcurrentHashMap<>();

    // ✅ application.properties: admin.token.ttl-minutes 값 사용 (기본 30분)
    private final Duration tokenTtl;

    public AdminTokenStore(@Value("${admin.token.ttl-minutes:30}") long ttlMinutes) {
        // ttlMinutes가 0 이하로 들어오면 안전하게 기본값 30분 처리
        long safe = ttlMinutes > 0 ? ttlMinutes : 30;
        this.tokenTtl = Duration.ofMinutes(safe);
    }

    /**
     * ✅ roleCode 정규화
     * - "ROLE_SUPER_ADMIN" -> "SUPER_ADMIN"
     * - "1" -> "SUPER_ADMIN"  (요구사항: SUPER_ADMIN("1")일 때만)
     */
    private static String normalizeRoleCode(String roleCode) {
        if (roleCode == null) return "";
        String r = roleCode.trim().replaceFirst("^ROLE_", "");
        if ("1".equals(r)) return "SUPER_ADMIN";
        return r;
    }

    public void save(String token, String loginId, String roleCode) {
        String normalized = normalizeRoleCode(roleCode);
        long expiresAtMs = Instant.now().plus(tokenTtl).toEpochMilli();

        store.put(token, new TokenInfo(token, loginId, normalized, expiresAtMs));
    }

    public Optional<TokenInfo> find(String token) {
        if (token == null || token.isBlank()) return Optional.empty();

        TokenInfo info = store.get(token);
        if (info == null) return Optional.empty();

        long now = Instant.now().toEpochMilli();
        if (info.isExpired(now)) {
            store.remove(token); // ✅ 만료 토큰 제거
            return Optional.empty();
        }
        return Optional.of(info);
    }

    public void delete(String token) {
        if (token != null && !token.isBlank()) store.remove(token);
    }

    public record TokenInfo(String token, String loginId, String roleCode, long expiresAtMs) {
        public boolean isExpired(long nowMs) {
            return nowMs >= expiresAtMs;
        }

        public Collection<? extends GrantedAuthority> authorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
        }
    }
}
