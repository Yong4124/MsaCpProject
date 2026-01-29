package com.example.personalJobs.service;

import com.example.personalJobs.dto.*;
import com.example.personalJobs.entity.Application;
import com.example.personalJobs.entity.Resume;
import com.example.personalJobs.repository.ApplicationRepository;
import com.example.personalJobs.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ApplicationRepository applicationRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeService resumeService;

    // ✅ 공고 상세 API (게이트웨이 기준) - 너 프로젝트에 맞게 경로만 바꾸면 됨
    @Value("${jobs.api.base-url:http://localhost:8000}")
    private String jobsApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * ✅ 초안 조회
     * - M300이 있으면: M300.status + (M300이 가리키는 M110으로 폼 채움)
     * - M300이 없으면: exists=false (원하면 최신 이력서 자동 채움도 가능)
     */
    @Transactional(readOnly = true)
    public ApplyDraftResponse getDraft(Long jobId, Integer seqNoM100) {

        Optional<Application> opt = applicationRepository
                .findTopBySeqNoM210AndSeqNoM100AndDelYnOrderBySeqNoM300Desc(jobId, seqNoM100, "N");

        if (opt.isEmpty()) {
            // ✅ 여기서 최신 이력서 자동 채움 하고 싶으면 data에 넣어주면 됨(선택)
            return new ApplyDraftResponse(false, "NONE", null, null);
        }

        Application a = opt.get();

        String status = (a.getReviewStatus() == null || a.getReviewStatus().isBlank())
                ? "TEMP"
                : a.getReviewStatus();

        ApplyRequest data = null;

        // ✅ M300이 가리키는 이력서(M110) 불러와서 폼 데이터로 변환
        if (a.getSeqNoM110() != null) {
            data = resumeService.getResumeAsApplyRequestByM110(a.getSeqNoM110()).orElse(null);
        }

        return new ApplyDraftResponse(true, status, a.getSeqNoM300(), data);
    }

    /**
     * ✅ 임시저장 = "TEMP"
     * - ApplyRequest 내용을 Resume(M110)로 저장
     * - Application(M300)은 jobId+m100 기준으로 upsert 하면서 seqNoM110만 교체
     */
    @Transactional
    public ApplySaveResponse tempSave(Long jobId, Integer seqNoM100, ApplyRequest req) {
        // ✅ 임시저장은 기존 것을 찾아서 UPDATE (기존 로직 유지)
        return upsertTemp(jobId, seqNoM100, req);
    }

    /**
     * ✅ 제출 = "SUBMITTED"
     * ✅ 수정: 항상 새로운 Application 생성 (중복 지원 허용)
     */
    @Transactional
    public ApplySaveResponse submit(Long jobId, Integer seqNoM100, ApplyRequest req) {
        // ✅ 수정: submit 시에는 항상 새로운 Application 생성
        return createNew(jobId, seqNoM100, req, "SUBMITTED");
    }

    /**
     * ✅ 임시저장 전용: 기존 것을 찾아서 UPDATE
     */
    private ApplySaveResponse upsertTemp(Long jobId, Integer seqNoM100, ApplyRequest req) {
        // 1) Resume(M110) 저장(새 버전 생성)
        Resume savedResume = resumeService.saveResumeFromApplyRequest(seqNoM100, req);

        // 2) Application(M300) upsert (jobId + m100 기준 최신 1개 갱신)
        Application a = applicationRepository
                .findTopBySeqNoM210AndSeqNoM100AndDelYnOrderBySeqNoM300Desc(jobId, seqNoM100, "N")
                .orElseGet(Application::new);

        a.setSeqNoM210(jobId);
        a.setSeqNoM100(seqNoM100);
        a.setSeqNoM110(savedResume.getSeqNoM110());
        a.setReviewStatus("TEMP");
        a.setDelYn("N");

        Application saved = applicationRepository.save(a);

        return new ApplySaveResponse(true, "TEMP", saved.getSeqNoM300());
    }

    /**
     * ✅ 제출 전용: 항상 새로운 Application 생성
     */
    private ApplySaveResponse createNew(Long jobId, Integer seqNoM100, ApplyRequest req, String status) {
        // 1) Resume(M110) 저장(새 버전 생성)
        Resume savedResume = resumeService.saveResumeFromApplyRequest(seqNoM100, req);

        // 2) ✅ 핵심 수정: 항상 새로운 Application 생성
        Application a = new Application();
        a.setSeqNoM210(jobId);
        a.setSeqNoM100(seqNoM100);
        a.setSeqNoM110(savedResume.getSeqNoM110());
        a.setReviewStatus(status);
        a.setDelYn("N");

        Application saved = applicationRepository.save(a);

        return new ApplySaveResponse(true, status, saved.getSeqNoM300());
    }

    // =========================================================
    // ✅ ✅ ✅ 추가: 지원현황(카드 목록) 조회
    // =========================================================

    /**
     * ✅ 내 지원현황 목록
     * - t_jb_m300 기반 (TEMP / SUBMITTED / 취소)
     * - 공고 회사명/공고명/로고는 job_id로 공고 상세 API에서 가져옴
     */
    @Transactional(readOnly = true)
    public MyApplyListResponse getMyApplyList(Integer seqNoM100, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Application> p = applicationRepository
                .findBySeqNoM100AndDelYnOrderBySeqNoM300Desc(seqNoM100, "N", pageable);

        List<MyApplyItemDto> items = new ArrayList<>();

        for (Application a : p.getContent()) {

            // 상태 텍스트 (2번째 화면 뱃지용)
            String statusText = toStatusText(a.getReviewStatus(), a.getCancelStatus());

            // 공고 정보 붙이기 (실패해도 목록은 뜨게)
            JobDetailDto job = null;
            try {
                String url = jobsApiBaseUrl + "/api/public/jobs/{jobId}";
                job = restTemplate.getForObject(url, JobDetailDto.class, a.getSeqNoM210());
            } catch (Exception ignore) {}

            items.add(new MyApplyItemDto(
                    a.getSeqNoM300(),
                    a.getSeqNoM210(),          // jobId
                    a.getSeqNoM110(),          // resumeId
                    statusText,
                    a.getReviewStatus(),
                    a.getCancelStatus(),
                    job != null ? job.getCompanyName() : null,
                    job != null ? job.getTitle() : null,
                    job != null ? job.getLogoPath() : null
            ));
        }

        return new MyApplyListResponse(
                items,
                p.getNumber(),
                p.getTotalPages(),
                p.getTotalElements()
        );
    }

    private String toStatusText(String reviewStatus, String cancelStatus) {
        // cancel_status가 Y 같은 값으로 들어오면 여기서 판단
        if (cancelStatus != null && !cancelStatus.isBlank()) return "지원취소";
        if (reviewStatus == null || reviewStatus.isBlank()) return "임시저장";
        if ("TEMP".equalsIgnoreCase(reviewStatus)) return "임시저장";
        if ("SUBMITTED".equalsIgnoreCase(reviewStatus)) return "제출완료";
        return reviewStatus; // 혹시 다른 값이면 그대로
    }

    // 공고 상세 API 응답(JSON) 매핑용 (네가 준 JSON 기준)
    public static class JobDetailDto {
        private Long id;
        private Long companyId;
        private String title;
        private String companyName;
        private String logoPath;

        public Long getId() { return id; }
        public Long getCompanyId() { return companyId; }
        public String getTitle() { return title; }
        public String getCompanyName() { return companyName; }
        public String getLogoPath() { return logoPath; }

        public void setId(Long id) { this.id = id; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
        public void setTitle(String title) { this.title = title; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
    }
}
