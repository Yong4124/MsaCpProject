package com.example.admin.config;

import com.example.admin.security.AdminTokenAuthFilter;
import com.example.admin.security.AdminTokenStore;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AdminTokenStore tokenStore) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // ✅ 토큰 기반이면 세션 사용 안 함
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ 인증/인가 실패 응답을 명확히(프론트 디버깅 쉬움)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) -> {
                            // 인증 안 됨(토큰 없음/무효)
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"message\":\"인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            // 인증은 됐는데 권한 부족
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"message\":\"권한이 없습니다.\"}");
                        })
                )

                // ✅ [B 방식 핵심] Bearer 토큰 -> Authentication 세팅 필터 등록
                // UsernamePasswordAuthenticationFilter 전에 실행되게 두면 안전함
                .addFilterBefore(new AdminTokenAuthFilter(tokenStore), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // =========================
                        // 1) 공개(permitAll)
                        // =========================
                        .requestMatchers(
                                new AntPathRequestMatcher("/admin"),       // 로그인 화면(프로젝트 라우트 기준)
                                new AntPathRequestMatcher("/admin.html"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/favicon.ico")
                        ).permitAll()

                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()

                        // 로그인 API는 공개
                        .requestMatchers(HttpMethod.POST, "/api/admin/login").permitAll()

                        // =========================
                        // 2) 관리자 화면: 로그인한 사용자만
                        // =========================
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).authenticated()

                        // =========================
                        // 3) 관리자 관리 API 권한
                        // =========================
                        // 목록/상세 조회는 "로그인만" 하면 OK
                        .requestMatchers(HttpMethod.GET, "/api/admin/users/**").authenticated()

                        // 등록/수정/삭제는 SUPER_ADMIN만
                        .requestMatchers(HttpMethod.POST, "/api/admin/users").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/users/**").hasRole("SUPER_ADMIN")

                        // 그 외 /api/admin/** 도 로그인 필요
                        .requestMatchers("/api/admin/**").authenticated()

                        // =========================
                        // 4) 나머지
                        // =========================
                        .anyRequest().permitAll()
                )

                // ✅ h2-console frame 허용
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    // ✅ PBKDF2WithHmacSHA512 PasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        String secret = "change-me-secret";   // 운영에선 환경변수/설정으로 빼기 권장
        int saltLength = 16;
        int iterations = 185000;

        return new Pbkdf2PasswordEncoder(
                secret,
                saltLength,
                iterations,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512
        );
    }
}
