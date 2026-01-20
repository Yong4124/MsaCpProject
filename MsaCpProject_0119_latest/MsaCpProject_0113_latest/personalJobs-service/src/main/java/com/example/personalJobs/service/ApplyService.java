package com.example.personalJobs.service;

import com.example.personalJobs.dto.ApplyDraftResponse;
import com.example.personalJobs.dto.ApplyRequest;
import com.example.personalJobs.dto.ApplySaveResponse;
import com.example.personalJobs.entity.Application;
import com.example.personalJobs.entity.Resume;
import com.example.personalJobs.repository.ApplicationRepository;
import com.example.personalJobs.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ApplicationRepository applicationRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeService resumeService;

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
        return upsert(jobId, seqNoM100, req, "TEMP");
    }

    /**
     * ✅ 제출 = "SUBMITTED"
     */
    @Transactional
    public ApplySaveResponse submit(Long jobId, Integer seqNoM100, ApplyRequest req) {
        return upsert(jobId, seqNoM100, req, "SUBMITTED");
    }

    private ApplySaveResponse upsert(Long jobId, Integer seqNoM100, ApplyRequest req, String status) {

        // 1) Resume(M110) 저장(새 버전 생성)
        Resume savedResume = resumeService.saveResumeFromApplyRequest(seqNoM100, req);

        // 2) Application(M300) upsert (jobId + m100 기준 최신 1개 갱신)
        Application a = applicationRepository
                .findTopBySeqNoM210AndSeqNoM100AndDelYnOrderBySeqNoM300Desc(jobId, seqNoM100, "N")
                .orElseGet(Application::new);

        a.setSeqNoM210(jobId);
        a.setSeqNoM100(seqNoM100);
        a.setSeqNoM110(savedResume.getSeqNoM110()); // ✅ 핵심: JSON이 아니라 M110 FK 저장
        a.setReviewStatus(status);
        a.setDelYn("N");

        Application saved = applicationRepository.save(a);

        return new ApplySaveResponse(true, status, saved.getSeqNoM300());
    }
}
