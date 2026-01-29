package com.example.personalJobs.service;

import com.example.personalJobs.dto.*;
import com.example.personalJobs.entity.Application;
import com.example.personalJobs.entity.Resume;
import com.example.personalJobs.repository.ApplicationRepository;
import com.example.personalJobs.repository.ResumeRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ApplicationRepository applicationRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeService resumeService;

    @Value("${jobs.api.base-url:http://localhost:8000}")
    private String jobsApiBaseUrl;

    private final ObjectMapper om = new ObjectMapper();

    // Draft 조회
    @Transactional(readOnly = true)
    public ApplyDraftResponse getDraft(Long jobId, Integer seqNoM100) {
        Optional<Application> opt = applicationRepository
                .findTopBySeqNoM210AndSeqNoM100AndDelYnInOrderBySeqNoM300Desc(jobId, seqNoM100, List.of("N", "Y"));

        if (opt.isPresent()) {
            Application a = opt.get();
            String status = (a.getReviewStatus() == null || a.getReviewStatus().isBlank()) ? "TEMP" : a.getReviewStatus();
            ApplyRequest data = null;
            if (a.getSeqNoM110() != null) {
                data = resumeService.getResumeAsApplyRequestByM110(a.getSeqNoM110()).orElse(null);
            }
            return new ApplyDraftResponse(true, status, a.getSeqNoM300(), a.getSeqNoM110(), data);
        }

        Optional<ApplyRequest> latestResume = resumeService.getLatestResumeAsApplyRequest(seqNoM100);
        return latestResume.map(r -> new ApplyDraftResponse(false, "NONE", null, null, r))
                .orElseGet(() -> new ApplyDraftResponse(false, "NONE", null, null, null));
    }

    // 임시 저장
    @Transactional
    public ApplySaveResponse tempSave(Long jobId, Integer seqNoM100, ApplyRequest req) {

        // 1) Resume 저장
        Resume savedResume = resumeService.saveResumeFromApplyRequest(seqNoM100, req);

        // 2) ✅ TEMP도 Application(t_jb_m300) row를 남긴다 (upsert)
        Application a = applicationRepository
                .findTopBySeqNoM210AndSeqNoM100AndDelYnInOrderBySeqNoM300Desc(jobId, seqNoM100, List.of("N", "Y"))
                .orElseGet(Application::new);

        // (안전장치) 이미 SUBMITTED인 건 TEMP로 덮지 않음
        if ("SUBMITTED".equalsIgnoreCase(safe(a.getReviewStatus())) && "N".equalsIgnoreCase(safe(a.getDelYn()))) {
            return new ApplySaveResponse(true, "SUBMITTED", a.getSeqNoM300());
        }

        a.setSeqNoM210(jobId);
        a.setSeqNoM100(seqNoM100);
        a.setSeqNoM110(savedResume.getSeqNoM110());
        a.setReviewStatus("TEMP");
        a.setCancelStatus("N");
        a.setDelYn("N");

        applicationRepository.save(a);

        return new ApplySaveResponse(true, "TEMP", a.getSeqNoM300());
    }

    // 최종 제출
// 최종 제출
    @Transactional
    public ApplySaveResponse submit(Long jobId, Integer seqNoM100, ApplyRequest req) {

        // ✅ TEMP는 허용, SUBMITTED만 막는다
        Application a = applicationRepository
                .findTopBySeqNoM210AndSeqNoM100AndDelYnInOrderBySeqNoM300Desc(
                        jobId, seqNoM100, List.of("N", "Y")
                )
                .orElseGet(Application::new);

        // ✅ 이미 "제출완료"인 경우만 차단
        if ("N".equalsIgnoreCase(safe(a.getDelYn()))
                && "SUBMITTED".equalsIgnoreCase(safe(a.getReviewStatus()))) {
            throw new IllegalStateException("이미 해당 공고에 지원하셨습니다.");
        }

        // 1) Resume 저장
        Resume savedResume = resumeService.saveResumeFromApplyRequest(seqNoM100, req);

        // 2) Application 업데이트/생성 → SUBMITTED 처리
        a.setSeqNoM210(jobId);
        a.setSeqNoM100(seqNoM100);
        a.setSeqNoM110(savedResume.getSeqNoM110());
        a.setReviewStatus("SUBMITTED");
        a.setCancelStatus("N");
        a.setDelYn("N");

        applicationRepository.save(a);

        return new ApplySaveResponse(true, "SUBMITTED", a.getSeqNoM300());
    }


    /**
     * ✅ 목록 조회
     * - 기존 네 로직 기반 유지
     * - TEMP도 이제 Application row가 생기니까 목록에 무조건 뜸
     */
    @Transactional(readOnly = true)
    public MyApplyListResponse getMyApplyList(Integer seqNoM100, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size);

        // ✅ Resume 기준 페이지 유지 (기존처럼)
        Page<Resume> p = resumeRepository.findBySeqNoM100AndDelYnOrderBySeqNoM110Desc(seqNoM100.longValue(), "N", pageable);

        // ✅ Application들 가져오기 (기존 네 repository 메서드 사용)
        List<Application> myApps = applicationRepository.findBySeqNoM100AndDelYn(seqNoM100, "N");

        // resumeId -> 최신 application 매핑
        Map<Long, Application> latestAppByResume = new HashMap<>();
        for (Application a : myApps) {
            Long rid = a.getSeqNoM110();
            if (rid == null) continue;
            Application cur = latestAppByResume.get(rid);
            if (cur == null || (a.getSeqNoM300() != null && cur.getSeqNoM300() != null && a.getSeqNoM300() > cur.getSeqNoM300())) {
                latestAppByResume.put(rid, a);
            }
        }

        // (같은 jobId 중복 제거하려면 유지)
        Set<Long> processedJobIds = new HashSet<>();
        List<MyApplyItemDto> items = new ArrayList<>();

        for (Resume r : p.getContent()) {

            Application a = latestAppByResume.get(r.getSeqNoM110());
            if (a == null) continue; // ✅ 이제 TEMP도 a가 있어야 함 (없으면 목록에 안 보이게)

            Long jobId = (a.getSeqNoM210() != null) ? a.getSeqNoM210() : 0L;
            if (jobId != null && jobId > 0 && processedJobIds.contains(jobId)) continue;

            JobDetailDto job = (jobId != null && jobId > 0) ? fetchJobDetail(jobId) : null;

            String reviewStatus = (a.getReviewStatus() == null || a.getReviewStatus().isBlank()) ? "TEMP" : a.getReviewStatus();
            String cancelStatus = (a.getCancelStatus() == null || a.getCancelStatus().isBlank()) ? "N" : a.getCancelStatus();
            String statusText = toStatusText(reviewStatus, cancelStatus);

            boolean closed = (job != null && Boolean.TRUE.equals(job.getClosed()));
            String ddayText = buildDdayText(job);

            items.add(new MyApplyItemDto(
                    a.getSeqNoM300(),
                    jobId,
                    r.getSeqNoM110(),
                    statusText,
                    reviewStatus,
                    cancelStatus,
                    job != null ? job.getCompanyName() : "-",
                    job != null ? job.getTitle() : "-",
                    job != null ? job.getLogoPath() : null,
                    job != null ? job.getWorkType() : null,
                    job != null ? job.getEmploymentType() : null,
                    job != null ? job.getIndustry() : null,
                    job != null ? job.getLevel() : null,
                    job != null ? job.getExperience() : null,
                    job != null ? job.getSalaryText() : null,
                    job != null ? job.getWorkingHours() : null,
                    job != null ? job.getLocation() : null,
                    closed,
                    ddayText
            ));

            if (jobId != null && jobId > 0) processedJobIds.add(jobId);
        }

        return new MyApplyListResponse(items, p.getNumber(), p.getTotalPages(), (int) p.getTotalElements());
    }

    private String toStatusText(String r, String c) {
        if ("Y".equalsIgnoreCase(c)) return "지원취소";
        if ("SUBMITTED".equalsIgnoreCase(r)) return "제출완료";
        return "임시저장";
    }

    private String buildDdayText(JobDetailDto job) {
        if (job == null) return null;
        if (Boolean.TRUE.equals(job.getClosed())) return "채용마감";
        if (job.getEndDate() == null) return "진행중";
        try {
            long diff = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(job.getEndDate()));
            return diff < 0 ? "마감" : "D-" + diff;
        } catch (Exception e) {
            return "진행중";
        }
    }

    private JobDetailDto fetchJobDetail(Long jobId) {
        String url = jobsApiBaseUrl + "/api/public/jobs/" + jobId;
        try {
            RestTemplate directRestTemplate = new RestTemplate();
            String body = directRestTemplate.getForObject(url, String.class);

            if (body == null) return null;
            JsonNode root = om.readTree(body);
            if (root.has("data") && !root.get("data").isNull()) root = root.get("data");
            if (root.has("data") && !root.get("data").isNull()) root = root.get("data");

            return om.treeToValue(root, JobDetailDto.class);
        } catch (Exception e) {
            System.err.println("API 호출 에러: " + e.getMessage());
            return null;
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JobDetailDto {
        private Long id;
        private Long companyId;
        private String title;
        private String companyName;
        private String logoPath;
        private String photoPath;

        @JsonProperty("jobForm")
        private String workType;

        @JsonProperty("jobType")
        private String employmentType;

        @JsonProperty("jobCategory")
        private String industry;

        @JsonProperty("roleLevel")
        private String level;

        private String experience;

        @JsonProperty("baseSalary")
        private String salaryText;

        @JsonProperty("workTime")
        private String workingHours;

        @JsonProperty("workLocation")
        private String location;

        @JsonProperty("closeYn")
        private String closedYn;

        private String endDate;

        private String companyIntro;
        private String positionSummary;
        private String skillQualification;
        private String benefits;
        private String notes;
        private String startDate;

        public Boolean getClosed() {
            return "Y".equalsIgnoreCase(closedYn);
        }
    }
}
