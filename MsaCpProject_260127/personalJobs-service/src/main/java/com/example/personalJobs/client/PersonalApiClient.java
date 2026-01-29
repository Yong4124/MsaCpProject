package com.example.personalJobs.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PersonalApiClient {

    private final RestTemplate restTemplate;

    // Eureka 등록명
    private static final String PERSONAL_SERVICE = "personal-service";

    @SuppressWarnings("unchecked")
    public Map<String, Object> myInfoByJwtCookie(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "JWT_TOKEN=" + jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> resp = restTemplate.exchange(
                "http://" + PERSONAL_SERVICE + "/api/personal/my-info",
                HttpMethod.GET,
                entity,
                Map.class
        );
        return resp.getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> myInfoByAuthorization(String authorization) {
        String token = authorization;
        if (token != null && token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isBlank()) {
            // personal-service가 Authorization을 안 받을 수도 있으니 Cookie도 같이
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            headers.add(HttpHeaders.COOKIE, "JWT_TOKEN=" + token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> resp = restTemplate.exchange(
                "http://" + PERSONAL_SERVICE + "/api/personal/my-info",
                HttpMethod.GET,
                entity,
                Map.class
        );
        return resp.getBody();
    }

    /**
     * ✅ 세션 fallback은 "JSESSIONID만" 보내는 게 아니라
     * 요청에 들어온 Cookie 헤더 전체를 personal-service로 그대로 전달하는 게 제일 안전함.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> myInfoBySession(HttpServletRequest request) {

        // 1) 브라우저가 보낸 Cookie 헤더를 그대로 복사 (가장 정확)
        String cookieHeader = request.getHeader(HttpHeaders.COOKIE);

        if (cookieHeader == null || cookieHeader.isBlank()) {
            throw new RuntimeException("No Cookie header on incoming request");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> resp = restTemplate.exchange(
                "http://" + PERSONAL_SERVICE + "/api/personal/my-info",
                HttpMethod.GET,
                entity,
                Map.class
        );
        return resp.getBody();
    }
}
