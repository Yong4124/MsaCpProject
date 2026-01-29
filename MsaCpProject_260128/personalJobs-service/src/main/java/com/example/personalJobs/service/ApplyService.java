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
import lombok.Data;
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
        Resume savedResume = resumeService.saveResumeFromApplyRequest(seqNoM100, req);
        // 혹시 몰라 강제 업데이트 (Repository에 updateTask가 없으면 이 줄은 지우고 저장하세요)
        try {
            resumeRepository.updateTask(savedResume.getSeqNoM110(), "TEMP_JOB_ID:" + jobId);
        } catch (Exception e) {
            // updateTask가 없으면 무시
        }
        return new ApplySaveResponse(true, "TEMP", 0L);
    }

    // 최종 제출
    @Transactional
    public ApplySaveResponse submit(Long jobId, Integer seqNoM100, ApplyRequest req) {
        Optional<Application> existing = applicationRepository
                .findTopBySeqNoM210AndSeqNoM100AndDelYnOrderBySeqNoM300Desc(jobId, seqNoM100, "N");
        if (existing.isPresent()) throw new IllegalStateException("이미 해당 공고에 지원하셨습니다.");

        Resume savedResume = resumeService.saveResumeFromApplyRequest(seqNoM100, req);
        Application a = applicationRepository
                .findTopBySeqNoM210AndSeqNoM100AndDelYnInOrderBySeqNoM300Desc(jobId, seqNoM100, List.of("N", "Y"))
                .orElseGet(Application::new);

        a.setSeqNoM210(jobId);
        a.setSeqNoM100(seqNoM100);
        a.setSeqNoM110(savedResume.getSeqNoM110());
        a.setReviewStatus("SUBMITTED");
        a.setDelYn("N");
        applicationRepository.save(a);
        return new ApplySaveResponse(true, "SUBMITTED", a.getSeqNoM300());
    }

    /**
     * ✅ 목록 조회 (로그 출력 기능 포함)
     */
    @Transactional(readOnly = true)
    public MyApplyListResponse getMyApplyList(Integer seqNoM100, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Resume> p = resumeRepository.findBySeqNoM100AndDelYnOrderBySeqNoM110Desc(seqNoM100.longValue(), "N", pageable);

        List<Application> myApps = applicationRepository.findBySeqNoM100AndDelYn(seqNoM100, "N");
        Set<Long> submittedJobIds = myApps.stream().map(Application::getSeqNoM210).collect(Collectors.toSet());
        Set<Long> processedJobIds = new HashSet<>();
        List<MyApplyItemDto> items = new ArrayList<>();

        for (Resume r : p.getContent()) {
            Optional<Application> optApp = applicationRepository
                    .findTopBySeqNoM110AndDelYnOrderBySeqNoM300Desc(r.getSeqNoM110(), "N");

            String statusText = "작성중";
            String reviewStatus = "TEMP";
            String cancelStatus = "N";
            Long jobId = 0L;
            JobDetailDto job = null;

            if (optApp.isPresent()) {
                Application a = optApp.get();
                jobId = a.getSeqNoM210();
                if (processedJobIds.contains(jobId)) continue;

                statusText = toStatusText(a.getReviewStatus(), a.getCancelStatus());
                reviewStatus = a.getReviewStatus();
                cancelStatus = a.getCancelStatus();
                job = fetchJobDetail(jobId);
                processedJobIds.add(jobId);

            } else {
                String task = r.getTask();
                if (task != null && task.startsWith("TEMP_JOB_ID:")) {
                    try {
                        jobId = Long.parseLong(task.replace("TEMP_JOB_ID:", "").trim());
                        if (submittedJobIds.contains(jobId) || processedJobIds.contains(jobId)) continue;

                        job = fetchJobDetail(jobId);
                        processedJobIds.add(jobId);
                    } catch (Exception e) {
                        jobId = 0L;
                    }
                }
            }

            if (jobId == 0L && job == null) continue;

            String createDate = (r.getInsertDate() != null && r.getInsertDate().length() >= 10)
                    ? r.getInsertDate().substring(0, 10) : "날짜없음";

            boolean closed = (job != null && Boolean.TRUE.equals(job.getClosed()));
            String ddayText = buildDdayText(job);

            items.add(new MyApplyItemDto(
                    optApp.map(Application::getSeqNoM300).orElse(0L),
                    jobId, r.getSeqNoM110(), statusText, reviewStatus, cancelStatus,
                    job != null ? job.getCompanyName() : "미제출 이력서",
                    job != null ? job.getTitle() : "작성일: " + createDate,
                    job != null ? job.getLogoPath() : null,
                    job != null ? job.getWorkType() : null,
                    job != null ? job.getEmploymentType() : null,
                    job != null ? job.getIndustry() : null,
                    job != null ? job.getLevel() : null,
                    job != null ? job.getExperience() : null,
                    job != null ? job.getSalaryText() : null,
                    job != null ? job.getWorkingHours() : null,
                    job != null ? job.getLocation() : null,
                    closed, ddayText
            ));
        }
        return new MyApplyListResponse(items, p.getNumber(), p.getTotalPages(), items.size());
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
            // 로드밸런서 무시하고 직접 통신
            RestTemplate directRestTemplate = new RestTemplate();
            String body = directRestTemplate.getForObject(url, String.class);

            // 🚨🚨🚨 [여기를 확인해주세요] 콘솔에 이 로그가 찍힙니다! 🚨🚨🚨
            System.out.println("👉 API 응답 데이터(ID:" + jobId + "): " + body);

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

        // API 필드명과 정확히 일치시키기
        @JsonProperty("jobForm")
        private String workType;           // 직업유형

        @JsonProperty("jobType")
        private String employmentType;     // 고용형태

        @JsonProperty("jobCategory")
        private String industry;           // 직종

        @JsonProperty("roleLevel")
        private String level;              // 직급

        private String experience;         // 경력

        @JsonProperty("baseSalary")
        private String salaryText;         // 기본급

        @JsonProperty("workTime")
        private String workingHours;       // 근무시간

        @JsonProperty("workLocation")
        private String location;           // 근무지

        @JsonProperty("closeYn")
        private String closedYn;

        private String endDate;

        // 추가 필드들 (화면에 필요하면 사용)
        private String companyIntro;       // 회사소개
        private String positionSummary;    // 포지션 요약
        private String skillQualification; // 자격요건
        private String benefits;           // 복리후생
        private String notes;              // 비고
        private String startDate;          // 시작일

        // closed 계산 로직
        public Boolean getClosed() {
            return "Y".equalsIgnoreCase(closedYn);
        }
    }
}