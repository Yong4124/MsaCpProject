package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 개발 편의상 CSRF 비활성화 (운영에서는 활성화 권장)
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 공개 페이지
                        .requestMatchers("/", "/home", "/auth/**", "/css/**", "/js/**", "/images/**", "/job/**").permitAll()

                        // 권한별 접근
                        .requestMatchers("/personal/**").hasRole("PERSONAL")
                        .requestMatchers("/company/**").hasRole("COMPANY")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 그 외는 로그인 필요
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login-process")
                        .usernameParameter("loginId")
                        .passwordParameter("password")

                        // ✅ 로그인 성공 시 권한별로 이동
                        .successHandler((request, response, authentication) -> {
                            var authorities = authentication.getAuthorities();
                            String targetUrl = "/";

                            boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            boolean isCompany = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMPANY"));
                            boolean isPersonal = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PERSONAL"));

                            if (isAdmin) {
                                targetUrl = "/admin/dashboard";
                            } else if (isCompany) {
                                targetUrl = "/company/dashboard";
                            } else if (isPersonal) {
                                targetUrl = "/personal/dashboard";
                            }

                            response.sendRedirect(request.getContextPath() + targetUrl);
                        })

                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
