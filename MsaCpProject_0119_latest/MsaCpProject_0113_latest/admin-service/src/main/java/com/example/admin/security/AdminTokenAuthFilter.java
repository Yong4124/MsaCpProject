package com.example.admin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class AdminTokenAuthFilter extends OncePerRequestFilter {

    private final AdminTokenStore tokenStore;

    public AdminTokenAuthFilter(AdminTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 이미 인증이 있으면 패스
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = resolveBearerToken(header);

        // token이 있는데 find()가 비어있다 = 만료/무효
        if (token != null) {
            var opt = tokenStore.find(token);
            if (opt.isEmpty()) {
                response.setStatus(401);
                return;
            }

            var info = opt.get();
            var auth = new UsernamePasswordAuthenticationToken(
                    info.loginId(),
                    null,
                    info.authorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);

    }

    private String resolveBearerToken(String header) {
        if (!StringUtils.hasText(header)) return null;
        if (!header.startsWith("Bearer ")) return null;
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
