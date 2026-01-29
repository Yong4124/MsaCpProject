package com.example.personalJobs.service;

import com.example.personalJobs.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    public Integer requireSeqNoM100(String tokenOnly) {
        return requireSeqNoM100(tokenOnly, null);
    }

    public Integer requireSeqNoM100(String token, String authorization) {
        String resolved = firstNonBlank(token, stripBearer(authorization));

        if (resolved == null || resolved.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 후 이용하세요.");
        }
        if (!jwtUtil.isTokenValid(resolved)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 후 이용하세요.");
        }

        String loginId = jwtUtil.extractLoginId(resolved);
        if (loginId == null || loginId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다.");
        }

        Integer seqNoM100 = jdbcTemplate.query(
                "SELECT seq_no_m100 FROM t_jb_m100 WHERE id = ? AND del_yn = 'N'",
                rs -> rs.next() ? rs.getInt(1) : null,
                loginId
        );

        if (seqNoM100 == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다.");
        }
        return seqNoM100;
    }

    public Integer requireSeqNoM100(String token, String authorization, jakarta.servlet.http.HttpServletRequest request) {
        return requireSeqNoM100(token, authorization);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private static String stripBearer(String authorization) {
        if (authorization == null) return null;
        String s = authorization.trim();
        if (s.toLowerCase().startsWith("bearer ")) return s.substring(7).trim();
        return s;
    }

    public Integer requireSeqNoM100(HttpServletRequest request) {
        String jwt = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                String name = c.getName();
                if ("JWT_TOKEN".equals(name) || "ACCESS_TOKEN".equals(name) || "TOKEN".equals(name)) {
                    if (c.getValue() != null && !c.getValue().isBlank()) {
                        jwt = c.getValue();
                        break;
                    }
                }
            }
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return requireSeqNoM100(jwt, authorization);
    }

}
