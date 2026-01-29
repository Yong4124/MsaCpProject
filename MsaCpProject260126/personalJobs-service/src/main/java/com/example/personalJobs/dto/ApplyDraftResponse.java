package com.example.personalJobs.dto;

public class ApplyDraftResponse {

    private boolean hasDraft;   // ← exists 대체
    private String status;      // NONE / TEMP / SUBMITTED
    private Long seqNoM300;     // applicationId 대체

    // ✅ 핵심 추가
    private Long resumeId;      // seq_no_m110

    private ApplyRequest data;

    public ApplyDraftResponse() {}

    // 기존 용도 호환
    public ApplyDraftResponse(boolean hasDraft, String status, Long seqNoM300, ApplyRequest data) {
        this.hasDraft = hasDraft;
        this.status = status;
        this.seqNoM300 = seqNoM300;
        this.data = data;
        this.resumeId = null;
    }

    // ⭐ draft 전용 (추천)
    public ApplyDraftResponse(boolean hasDraft, String status, Long seqNoM300, Long resumeId, ApplyRequest data) {
        this.hasDraft = hasDraft;
        this.status = status;
        this.seqNoM300 = seqNoM300;
        this.resumeId = resumeId;
        this.data = data;
    }

    public boolean isHasDraft() { return hasDraft; }
    public String getStatus() { return status; }
    public Long getSeqNoM300() { return seqNoM300; }
    public Long getResumeId() { return resumeId; }
    public ApplyRequest getData() { return data; }
}
