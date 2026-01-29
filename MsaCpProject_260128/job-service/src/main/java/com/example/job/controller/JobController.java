package com.example.job.controller;

import com.example.job.dto.ApplicantDto;
import com.example.job.dto.JobDto;
import com.example.job.model.CompanyMember;
import com.example.job.model.Job;
import com.example.job.repository.CompanyMemberRepository;
import com.example.job.service.ApplicantService;
import com.example.job.service.JobService;
import com.example.job.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final ApplicantService applicantService;
    private final JwtUtil jwtUtil;
    private final CompanyMemberRepository companyRepository;

    /**
     * 내 회사의 채용공고 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<JobDto>> getAllJobs(
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer companyId = jwtUtil.extractCompanyId(token);

        // ⭐ DB에서 최신 회사 정보 조회
        CompanyMember company = companyRepository.findById(companyId)
                .orElse(null);

        List<JobDto> jobs = jobService.getJobList(companyId.longValue());

        // ⭐ DB에서 가져온 최신 정보로 설정
        jobs.forEach(job -> {
            if (company != null) {
                job.setCompanyName(company.getCompanyName());
                job.setCeoName(company.getCeoName());
                job.setCompanyAddress(company.getCompanyAddress());
                job.setLogoPath(company.getLogoPath());  // ✅ 최신 로고 경로
            }
        });

        return ResponseEntity.ok(jobs);
    }

    /**
     * 채용공고 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobDetail(
            @PathVariable Long id,
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        JobDto job = jobService.getJobDtoById(id);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        // JWT 토큰이 있으면 회사 정보 설정
        if (token != null && !token.isBlank()) {
            try {
                Integer tokenCompanyId = jwtUtil.extractCompanyId(token);

                // 본인 회사의 공고인 경우
                if (job.getCompanyId() != null && job.getCompanyId().equals(tokenCompanyId)) {
                    // ⭐ DB에서 최신 회사 정보 조회
                    companyRepository.findById(tokenCompanyId).ifPresent(company -> {
                        job.setCompanyName(company.getCompanyName());
                        job.setCeoName(company.getCeoName());
                        job.setCompanyAddress(company.getCompanyAddress());
                        job.setLogoPath(company.getLogoPath());  // ✅ 최신 로고 경로
                    });
                }
            } catch (Exception e) {
                // JWT 파싱 실패 시 무시
            }
        }

        // 기본값 설정
        if (job.getCompanyName() == null || job.getCompanyName().isEmpty()) {
            job.setCompanyName("회사 정보 없음");
        }
        if (job.getCeoName() == null || job.getCeoName().isEmpty()) {
            job.setCeoName("대표 정보 없음");
        }
        if (job.getCompanyAddress() == null || job.getCompanyAddress().isEmpty()) {
            job.setCompanyAddress("주소 정보 없음");
        }
        if (job.getLogoPath() == null || job.getLogoPath().isEmpty()) {
            job.setLogoPath("/img/common/default_logo.png");
        }

        return ResponseEntity.ok(job);
    }

    /**
     * 채용공고 등록
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createJob(
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestBody Map<String, Object> payload) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer companyId = jwtUtil.extractCompanyId(token);

        // ⭐ DB에서 최신 회사 정보 조회
        CompanyMember company = companyRepository.findById(companyId)
                .orElse(null);

        JobDto created = jobService.createWithCompany(payload, companyId.longValue());

        // ⭐ DB에서 가져온 최신 정보로 설정
        if (company != null) {
            created.setCompanyName(company.getCompanyName());
            created.setCeoName(company.getCeoName());
            created.setCompanyAddress(company.getCompanyAddress());
            created.setLogoPath(company.getLogoPath());  // ✅ 최신 로고 경로
        }

        return ResponseEntity.ok(Map.of("success", true, "data", created));
    }

    /**
     * 채용공고 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateJob(
            @PathVariable Long id,
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestBody Job job) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer companyId = jwtUtil.extractCompanyId(token);

        // 본인 회사의 공고인지 확인
        JobDto existingJob = jobService.getJobDtoById(id);
        if (existingJob == null || !existingJob.getCompanyId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        JobDto updated = jobService.update(id, job);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "채용공고가 수정되었습니다.",
                "data", updated
        ));
    }

    /**
     * 채용공고 마감
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<Map<String, Object>> closeJob(
            @PathVariable Long id,
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer companyId = jwtUtil.extractCompanyId(token);

        // 본인 회사의 공고인지 확인
        JobDto existingJob = jobService.getJobDtoById(id);
        if (existingJob == null || !existingJob.getCompanyId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        jobService.close(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "채용공고가 마감되었습니다."
        ));
    }

    /**
     * 채용공고 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteJob(
            @PathVariable Long id,
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer companyId = jwtUtil.extractCompanyId(token);

        // 본인 회사의 공고인지 확인
        JobDto existingJob = jobService.getJobDtoById(id);
        if (existingJob == null || !existingJob.getCompanyId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        jobService.delete(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "채용공고가 삭제되었습니다."
        ));
    }

    /**
     * 특정 채용공고의 지원자 목록 조회
     */
    @GetMapping("/{jobId}/applicants")
    public ResponseEntity<List<ApplicantDto>> getApplicants(
            @PathVariable Long jobId,
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestParam(required = false) String status) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer companyId = jwtUtil.extractCompanyId(token);

        // 본인 회사의 공고인지 확인
        JobDto job = jobService.getJobDtoById(jobId);
        if (job == null || !job.getCompanyId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ApplicantDto> applicants = applicantService.getApplicants(jobId, status);
        return ResponseEntity.ok(applicants);
    }

    /**
     * 지원자 이력서
     */
    @GetMapping("/{jobId}/applicants/{applicantId}/resume")
    public ResponseEntity<ApplicantDto> getApplicantResume(
            @PathVariable Long jobId,
            @PathVariable Long applicantId,
            @CookieValue(value = "JWT_TOKEN", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer companyId = jwtUtil.extractCompanyId(token);

        // 본인 회사의 공고인지 확인
        JobDto job = jobService.getJobDtoById(jobId);
        if (job == null || !job.getCompanyId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ApplicantDto applicant = applicantService.getApplicant(applicantId);

        if (!applicant.getJobId().equals(jobId)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(applicant);
    }

    /**
     * 지원자 상태 변경
     */
    @PostMapping("/applicants/{applicantId}/status")
    public ResponseEntity<Map<String, Object>> updateApplicantStatus(
            @PathVariable Long applicantId,
            @CookieValue(value = "JWT_TOKEN", required = false) String token,
            @RequestBody Map<String, String> request) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String status = request.get("status");
        ApplicantDto updatedApplicant = applicantService.updateStatus(applicantId, status);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "상태가 변경되었습니다.",
                "data", updatedApplicant
        ));
    }
}