package com.example.job.controller;

import com.example.job.dto.ApplicantDto;
import com.example.job.dto.JobDto;
import com.example.job.service.ApplicantService;
import com.example.job.service.JobPublicService;
import com.example.job.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 공개 채용공고 API (인증 불필요)
 */
@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
public class JobPublicController {

    private final JobPublicService jobPublicService;
    private final ApplicantService applicantService;
    private final JwtUtil jwtUtil;

    /**
     * 공개된 채용공고 목록 조회
     * DB에 저장된 회사 정보를 그대로 반환 (JWT 토큰 불필요)
     */
    @GetMapping
    public ResponseEntity<List<JobDto>> getPublicJobList() {
        List<JobDto> jobs = jobPublicService.getPublicJobList();
        return ResponseEntity.ok(jobs);
    }

    /**
     * 공개된 채용공고 상세 조회
     * DB에 저장된 회사 정보를 그대로 반환 (JWT 토큰 불필요)
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobDetail(@PathVariable Long id) {
        JobDto job = jobPublicService.getPublicJobDetail(id);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        System.out.println("=== API Response Debug ===");
        System.out.println("ID: " + job.getId());
        System.out.println("CompanyName: " + job.getCompanyName());
        System.out.println("CompanyAddress: " + job.getCompanyAddress());
        System.out.println("LogoPath: " + job.getLogoPath());
        System.out.println("Title: " + job.getTitle());
        System.out.println("==========================");

        return ResponseEntity.ok(job);
    }

    /**
     * 채용공고 지원하기
     */
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<Map<String, Object>> applyToJob(
            @PathVariable Long jobId,
            @RequestBody ApplicantDto dto) {

        try {
            ApplicantDto saved = applicantService.saveApplicant(jobId, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "지원이 완료되었습니다.",
                    "data", saved
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "지원 처리 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 회원 타입 체크 (지원 가능 여부 확인)
     */
    @GetMapping("/check-member-type")
    public ResponseEntity<Map<String, Object>> checkMemberType(
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        Map<String, Object> result = new HashMap<>();

        if (token == null || token.isBlank()) {
            result.put("loggedIn", false);
            result.put("memberType", null);
            return ResponseEntity.ok(result);
        }

        try {
            Claims claims = jwtUtil.extractClaims(token);
            String memberType = claims.get("memberType", String.class);

            result.put("loggedIn", true);
            result.put("memberType", memberType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("loggedIn", false);
            result.put("memberType", null);
            return ResponseEntity.ok(result);
        }
    }
}