package com.example.job.controller;

import com.example.job.dto.ApplicantDto;
import com.example.job.dto.JobDto;
import com.example.job.model.Job;
import com.example.job.service.ApplicantService;
import com.example.job.service.JobPublicService;
import com.example.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 기업 관리자용 채용공고 API
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final ApplicantService applicantService;
    private final JobPublicService jobPublicService;

    /**
     * 채용공고 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<JobDto>> getAllJobs(@RequestParam(required = false) Long companyId) {
        List<JobDto> jobs = jobService.getJobList(companyId);
        return ResponseEntity.ok(jobs);
    }

    /**
     * 채용공고 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobDetail(@PathVariable Long id) {
        JobDto job = jobService.getJobDtoById(id);
        return ResponseEntity.ok(job);
    }

    /**
     * 채용공고 등록
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createJob(@RequestBody Job job) {
        JobDto created = jobService.create(job);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "채용공고가 등록되었습니다.",
                "data", created
        ));
    }

    /**
     * 채용공고 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateJob(
            @PathVariable Long id,
            @RequestBody Job job) {
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
    public ResponseEntity<Map<String, Object>> closeJob(@PathVariable Long id) {
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
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable Long id) {
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
            @RequestParam(required = false) String status) {
        List<ApplicantDto> applicants = applicantService.getApplicants(jobId, status);
        return ResponseEntity.ok(applicants);
    }

    /**
     * 지원자 이력서
     */
    @GetMapping("/{jobId}/applicants/{applicantId}/resume")
    public ResponseEntity<ApplicantDto> getApplicantResume(
            @PathVariable Long jobId,
            @PathVariable Long applicantId) {

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
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        ApplicantDto updatedApplicant = applicantService.updateStatus(applicantId, status);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "상태가 변경되었습니다.",
                "data", updatedApplicant
        ));
    }
}