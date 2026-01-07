package com.example.job.controller;

import com.example.job.dto.ApplicantDto;
import com.example.job.dto.JobDto;
import com.example.job.service.ApplicantService;
import com.example.job.service.JobPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 공개된 채용공고 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<JobDto>> getPublicJobList() {
        List<JobDto> jobs = jobPublicService.getPublicJobList();
        return ResponseEntity.ok(jobs);
    }

    /**
     * 공개된 채용공고 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobDetail(@PathVariable Long id) {
        JobDto job = jobPublicService.getPublicJobDetail(id);
        return ResponseEntity.ok(job);
    }

    /**
     * 채용공고 지원하기
     */
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<Map<String, Object>> applyToJob(
            @PathVariable Long jobId,
            @RequestBody ApplicantDto dto) {
        ApplicantDto saved = applicantService.saveApplicant(jobId, dto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "지원이 완료되었습니다.",
                "data", saved
        ));
    }
}