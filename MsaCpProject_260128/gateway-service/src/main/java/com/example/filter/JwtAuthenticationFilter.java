package com.example.filter;

import com.example.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ⭐ JWT 검증이 필요 없는 공개 경로
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            // 페이지
            "/",
            "/login",
            "/register",
            "/intro",
            "/search-id",
            "/search-pw",
            "/reset-pw",
            "/company/login",
            "/company/register",
            "/company/search-id",
            "/company/search-pw",
            "/jobs",  // 채용공고 목록 페이지
            "/admin",
            "/admin/**",

            // Personal API (회원가입/로그인)
            "/api/personal/register",
            "/api/personal/login",
            "/api/personal/check-id/**",
            "/api/personal/check-email/**",
            "/api/personal/send-verification",
            "/api/personal/send-email-verification",
            "/api/personal/verify-code",
            "/api/personal/find-id",
            "/api/personal/send-reset-token",
            "/api/personal/send-password-reset",
            "/api/personal/verify-password-reset",
            "/api/personal/verify-reset-token",
            "/api/personal/reset-password",
            "/api/personal/admin/**",

            // Company API (회원가입/로그인)
            "/api/company/register",
            "/api/company/login",
            "/api/company/check-id/**",
            "/api/company/check-email/**",
            "/api/company/send-verification",
            "/api/company/find-id",
            "/api/company/find-pw",
            "/api/company/reset-pw",                   // ⭐ 비밀번호 재설정 (토큰 기반)
            "/api/company/reset-password-request",
            "/api/company/reset-password",
            "/api/company/verify-code",
            "/api/company/admin/**",

            // Admin API (자체 인증 사용)
            "/api/admin/**",
            "/api/jobs/admin/**",
            "/api/company/admin/**",

            // 공개 채용공고 API
            "/api/public/**",

            // 정적 리소스
            "/css/**",
            "/js/**",
            "/img/**",
            "/uploads/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        System.out.println("🔍 Gateway Filter - 요청 경로: " + path);

        // ⭐ 공개 경로는 JWT 검증 없이 통과
        if (isPublicPath(path)) {
            System.out.println("✅ 공개 경로 - JWT 검증 생략");
            return chain.filter(exchange);
        }

        // ⭐ JWT 토큰 추출
        String token = extractToken(exchange);

        if (token == null) {
            System.out.println("❌ JWT 토큰 없음");
            return handleUnauthorized(exchange);
        }

        // ⭐ JWT 토큰 검증
        if (!jwtUtil.isTokenValid(token)) {
            System.out.println("❌ JWT 토큰 유효하지 않음");
            return handleUnauthorized(exchange);
        }

        // ⭐ 토큰에서 사용자 정보 추출
        String loginId = jwtUtil.extractLoginId(token);
        String memberType = jwtUtil.extractMemberType(token);

        System.out.println("✅ JWT 검증 성공 - loginId: " + loginId + ", memberType: " + memberType);

        // ⭐ 경로별 권한 체크
        if (!hasPermission(path, memberType)) {
            System.out.println("❌ 권한 없음 - " + memberType + "은(는) " + path + " 접근 불가");
            return handleForbidden(exchange);
        }

        // ⭐ 검증된 사용자 정보를 헤더에 추가하여 다음 서비스로 전달
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", loginId)
                .header("X-User-Type", memberType)
                .header("X-Auth-Token", token)
                .build();

        System.out.println("✅ 요청 헤더 추가 완료 - 다음 서비스로 전달");

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    // 공개 경로 확인
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath -> {
            if (publicPath.endsWith("/**")) {
                String prefix = publicPath.substring(0, publicPath.length() - 3);
                return path.startsWith(prefix);
            }
            return path.equals(publicPath);  // ⭐ startsWith 제거 (정확한 매칭)
        });
    }

    // 쿠키에서 JWT 토큰 추출
    private String extractToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("JWT_TOKEN");
        return cookie != null ? cookie.getValue() : null;
    }

    // 경로별 권한 체크
    private boolean hasPermission(String path, String memberType) {
        // Personal 회원 전용
        if (path.startsWith("/api/personal/") ||
                path.startsWith("/mypage") ||
                path.startsWith("/applystatus") ||
                path.startsWith("/api/resume/") ||
                path.startsWith("/api/apply/")) {
            return "PERSONAL".equals(memberType);
        }

        // Company 회원 전용 (주의: /api/jobs는 Company용, /api/public/jobs는 공개)
        if (path.startsWith("/api/company/") ||
                path.startsWith("/company/mypage") ||
                path.startsWith("/api/jobs")) {
            return "COMPANY".equals(memberType);
        }

        // Admin은 이미 공개 경로에서 처리됨 (자체 인증 사용)

        // 기타 경로는 모두 허용
        return true;
    }

    // 401 Unauthorized 응답
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    // 403 Forbidden 응답
    private Mono<Void> handleForbidden(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100; // 가장 먼저 실행되도록 우선순위 설정
    }
}